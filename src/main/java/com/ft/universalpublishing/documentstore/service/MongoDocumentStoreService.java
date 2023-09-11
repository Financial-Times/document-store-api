package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.IDStreamingException;
import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDocumentStoreService {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDocumentStoreService.class);
  private static final String IDENT_AUTHORITY = "identifiers.authority";
  private static final String IDENT_VALUE = "identifiers.identifierValue";

  private final MongoDatabase db;
  private final ExecutorService exec;
  private boolean indexed;
  private final Runnable reindexer = this::applyIndexes;

  public MongoDocumentStoreService(final MongoDatabase db, ExecutorService exec) {
    this.db = db;
    this.exec = exec;
    exec.submit(reindexer);
  }

  public boolean isConnected() {
    boolean connected = false;

    try {
      Document commandResult = db.runCommand(Document.parse("{ serverStatus : 1 }"));
      connected = !commandResult.isEmpty();
    } catch (MongoException e) {
      LOG.warn("Cannot verify MongoDB connection", e);
    }

    if (connected && !indexed) {
      // maybe we made a new connection, ensure indexes are created
      exec.submit(reindexer);
    } else if (!connected) {
      // we lost a connection, assume indexes are not up to date
      indexed = false;
    }

    return connected;
  }

  public boolean isIndexed() {
    return indexed;
  }

  public Map<String, Object> findByUuid(String resourceType, UUID uuid) {
    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);
      Document foundDocument =
          dbCollection.find().filter(Filters.eq("uuid", uuid.toString())).first();
      if (foundDocument == null) {
        throw new DocumentNotFoundException(uuid);
      }

      foundDocument.remove("_id");
      return foundDocument;
    } catch (MongoSocketException | MongoTimeoutException e) {
      LOG.error(
          "MongoDB connection timed out or caused a socket exception during delete, please check Atlas MongoDB! Collection {}, uuids {}",
          resourceType,
          uuid,
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error(
          "Failed to find document in MongoDB! Collection {}, uuids {}", resourceType, uuid, e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public List<Document> findByUuids(String resourceType, Set<UUID> uuids) {
    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);

      Iterable<Document> results =
          dbCollection
              .find()
              .filter(
                  Filters.in(
                      "uuid", uuids.stream().map(UUID::toString).collect(Collectors.toList())));

      Map<UUID, Document> mappedResults = new HashMap<>();
      results.forEach(doc -> mappedResults.put(UUID.fromString((String) doc.get("uuid")), doc));

      // preserve the order of the queried UUIDs in the found documents
      List<Document> documents = new LinkedList<>();
      uuids.forEach(
          uuid -> {
            Document doc = mappedResults.get(uuid);
            if (doc != null) {
              doc.remove("_id");
              documents.add(doc);
            }
          });

      return documents;
    } catch (MongoSocketException | MongoTimeoutException e) {
      LOG.error(
          "MongoDB connection timed out or caused a socket exception during delete, please check Atlas MongoDB! Collection {}, uuids {}",
          resourceType,
          uuids,
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error(
          "Failed to find document(s) in Mongo! Collection {}, uuids {}", resourceType, uuids, e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public Map<String, Object> findByIdentifier(
      String resourceType, String authority, String identifierValue) {
    Bson filter =
        Filters.and(
            Filters.eq("identifiers.authority", authority),
            Filters.eq("identifiers.identifierValue", identifierValue));

    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);
      Document found = null;

      for (Document doc : dbCollection.find(filter).limit(2)) {
        if (found == null) {
          found = doc;
          found.remove("_id");
        } else {
          LOG.warn(
              "found too many results for collection {} identifier {}:{}: at least {} and {}",
              resourceType,
              authority,
              identifierValue,
              found,
              doc);
          throw new QueryResultNotUniqueException();
        }
      }

      return found;
    } catch (MongoException e) {
      LOG.error(
          "Failed to find document in MongoDB! Collection {}, authority {}, identifierValue {}",
          resourceType,
          authority,
          identifierValue,
          e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public void delete(String resourceType, UUID uuid) {
    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);
      DeleteResult deleteResult = dbCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

      if (deleteResult.getDeletedCount() == 0) {
        throw new DocumentNotFoundException(uuid);
      }

    } catch (MongoSocketException | MongoTimeoutException e) {
      LOG.error(
          "MongoDB connection timed out or caused a socket exception during delete, please check Atlas MongoDB! Collection {}, uuid {}",
          resourceType,
          uuid,
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error(
          "Failed to delete document to MongoDB! Collection {}, uuid {}", resourceType, uuid, e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public DocumentWritten write(String resourceType, Map<String, Object> content) {
    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);
      final String uuid = (String) content.get("uuid");
      Document document = new Document(content);
      UpdateResult updateResult =
          dbCollection.replaceOne(
              Filters.eq("uuid", uuid), document, new ReplaceOptions().upsert(true));
      if (updateResult.getUpsertedId() == null) {
        return DocumentWritten.updated(document);
      }
      return DocumentWritten.created(document);
    } catch (MongoSocketException | MongoTimeoutException e) {
      LOG.error(
          "MongoDB connection timed out or caused a socket exception during write, please check Atlas MongoDB! Collection {}, uuid {}",
          resourceType,
          content.get("uuid"),
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error(
          "Failed to write document to MongoDB! Collection {}, uuid {}",
          resourceType,
          content.get("uuid"),
          e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public void applyIndexes() {
    applyIndexForCollection("content");
    applyIndexForCollection("internalcomponents");
    indexed = true;
  }

  @SuppressWarnings("rawtypes")
  private void applyIndexForCollection(String collection) {
    MongoCollection mongoCollection = db.getCollection(collection);
    LOG.info("Creating UUID index on collection [{}]", collection);
    String index = createUuidIndex(mongoCollection);
    LOG.info("Created UUID index [{}] on collection [{}]", index, collection);

    LOG.info("Creating identifier index on collection [{}]", collection);
    index = createIdentifierIndex(mongoCollection);
    LOG.info("Created identifier index [{}] on collection [{}]", index, collection);
  }

  private String createUuidIndex(MongoCollection<?> collection) {
    return collection.createIndex(
        new Document("uuid", 1), new IndexOptions().background(true).unique(true));
  }

  private String createIdentifierIndex(MongoCollection<?> collection) {
    Document queryByIdentifierIndex = new Document();
    queryByIdentifierIndex.put(IDENT_AUTHORITY, 1);
    queryByIdentifierIndex.put(IDENT_VALUE, 1);
    return collection.createIndex(
        queryByIdentifierIndex, new IndexOptions().name("identifiers_authority_values"));
  }

  public void findUUIDs(String resourceType, boolean includeSource, OutputStream outputStream) {
    MongoCollection<Document> collection = db.getCollection(resourceType);
    try (MongoCursor<Document> cursor = getFindUUIDsQuery(collection, includeSource).iterator()) {

      try {
        while (cursor.hasNext()) {
          Document document = cursor.next();
          outputStream.write((document.toJson() + "\n").getBytes());
        }
        outputStream.flush();
      } catch (IOException e) {
        LOG.error("Error occurred while trying to return ids");
        throw new IDStreamingException(resourceType);
      }
    }
  }

  private FindIterable<Document> getFindUUIDsQuery(
      MongoCollection<Document> collection, boolean includeSource) {
    List<Bson> projections =
        new ArrayList<>(Arrays.asList(Projections.include("uuid"), Projections.excludeId()));
    if (includeSource) {
      projections.add(Projections.include(IDENT_AUTHORITY));
    }
    return collection.find().projection(Projections.fields(projections));
  }
}
