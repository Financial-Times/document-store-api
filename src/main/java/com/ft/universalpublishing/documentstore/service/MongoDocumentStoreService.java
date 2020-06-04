package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.IDStreamingException;
import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDocumentStoreService {

  private static final String LISTS_COLLECTION = "lists";
  private static final Logger LOG = LoggerFactory.getLogger(MongoDocumentStoreService.class);
  private static final String IDENT_AUTHORITY = "identifiers.authority";
  private static final String IDENT_VALUE = "identifiers.identifierValue";
  private static final String CONCEPT_UUID = "concept.uuid";
  private static final String LIST_TYPE = "listType";

  private final MongoDatabase db;
  private ExecutorService exec;
  private boolean indexed;
  private Runnable reindexer = this::applyIndexes;

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

  public List<Document> filterLists(
      String resourceType, UUID[] conceptUUIDs, String listType, String searchTerm) {

    List<Bson> queryFilters = new ArrayList<>();
    UUID[] resolvedConceptUUIDs = Optional.ofNullable(conceptUUIDs).orElse(new UUID[] {});
    String[] conceptUUIDStrings =
        Arrays.asList(resolvedConceptUUIDs).stream()
            .map(uuid -> uuid.toString())
            .toArray(String[]::new);

    if (conceptUUIDStrings.length > 0) {
      Bson filterByConceptUUID = Filters.in("concept.uuid", conceptUUIDStrings);
      queryFilters.add(filterByConceptUUID);
    }
    if (listType != null) {
      Bson filterByListType = Filters.eq("listType", listType);
      queryFilters.add(filterByListType);
    }
    if (searchTerm != null) {
      Pattern regexSearchTerm = Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE);
      Bson filterByTitle = Filters.eq("title", regexSearchTerm);
      queryFilters.add(filterByTitle);
    }

    Bson filter = Filters.and(queryFilters);

    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);
      Iterable<Document> results;
      if (conceptUUIDStrings.length == 0 && listType == null && searchTerm == null) {
        results = dbCollection.find();
      } else {
        results = dbCollection.find(filter);
      }

      ArrayList<Document> documents = new ArrayList<>();
      results.forEach(
          doc -> {
            if (doc != null) {
              doc.remove("_id");
              documents.add(doc);
            }
          });
      return documents;

    } catch (MongoSocketException | MongoTimeoutException e) {
      LOG.error(
          "MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}",
          resourceType,
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error("Failed to find document(s) in Mongo! Collection {}", resourceType, e);
      throw new ExternalSystemInternalServerException(e);
    }
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
          "MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}, uuids {}",
          resourceType,
          uuid,
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error("Failed to find document in Mongo! Collection {}, uuids {}", resourceType, uuid, e);
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
          "MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}, uuids {}",
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
          "Failed to find document in Mongo! Collection {}, authority {}, identifierValue {}",
          resourceType,
          authority,
          identifierValue,
          e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public Map<String, Object> findByConceptAndType(
      String resourceType, UUID[] conceptUUIDs, String listType) {
    String[] conceptUUIDStrings =
        Arrays.asList(conceptUUIDs).stream().map(uuid -> uuid.toString()).toArray(String[]::new);
    Bson filter =
        Filters.and(
            Filters.in("concept.uuid", conceptUUIDStrings), Filters.eq("listType", listType));

    try {
      MongoCollection<Document> dbCollection = db.getCollection(resourceType);
      Document found = null;

      // sorting the results by lastModified so we always return the most recently
      // modified list
      found =
          dbCollection.find(filter).sort(new BasicDBObject("publishedDate", -1)).limit(1).first();

      return found;
    } catch (MongoException e) {
      LOG.error(
          "Failed to find document in Mongo! Collection {}, uuid {}, listType {}",
          resourceType,
          conceptUUIDs,
          listType,
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
          "MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}, uuid {}",
          resourceType,
          uuid,
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error(
          "Failed to delete document to Mongo! Collection {}, uuid {}", resourceType, uuid, e);
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
              Filters.eq("uuid", uuid), document, new UpdateOptions().upsert(true));
      if (updateResult.getUpsertedId() == null) {
        return DocumentWritten.updated(document);
      }
      return DocumentWritten.created(document);
    } catch (MongoSocketException | MongoTimeoutException e) {
      LOG.error(
          "MongoDB connection timed out or caused a socket exception during write, please check MongoDB! Collection {}, uuid {}",
          resourceType,
          content.get("uuid"),
          e);
      throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
    } catch (MongoException e) {
      LOG.error(
          "Failed to write document to Mongo! Collection {}, uuid {}",
          resourceType,
          content.get("uuid"),
          e);
      throw new ExternalSystemInternalServerException(e);
    }
  }

  public void applyIndexes() {
    applyIndexForCollection("content");
    applyIndexForCollection("internalcomponents");
    applyIndexForListCollection();
    indexed = true;
  }

  @SuppressWarnings("rawtypes")
  private void applyIndexForListCollection() {
    MongoCollection lists = db.getCollection(LISTS_COLLECTION);
    LOG.info("Creating UUID index on collection [{}]", LISTS_COLLECTION);
    createUuidIndex(lists);
    LOG.info("Created UUID index on collection [{}]", LISTS_COLLECTION);
    createConceptAndListTypeIndex(lists);
  }

  @SuppressWarnings("rawtypes")
  private void applyIndexForCollection(String collection) {
    MongoCollection mongoCollection = db.getCollection(collection);
    LOG.info("Creating UUID index on collection [{}]", collection);
    createUuidIndex(mongoCollection);
    LOG.info("Created UUID index on collection [{}]", collection);
    createIdentifierIndex(mongoCollection);
  }

  private void createUuidIndex(MongoCollection<?> collection) {
    collection.createIndex(
        new Document("uuid", 1), new IndexOptions().background(true).unique(true));
  }

  private void createIdentifierIndex(MongoCollection<?> collection) {
    Document queryByIdentifierIndex = new Document();
    queryByIdentifierIndex.put(IDENT_AUTHORITY, 1);
    queryByIdentifierIndex.put(IDENT_VALUE, 1);
    collection.createIndex(queryByIdentifierIndex);
  }

  private void createConceptAndListTypeIndex(MongoCollection<?> collection) {
    Document queryByIdentifierIndex = new Document();
    queryByIdentifierIndex.put(CONCEPT_UUID, 1);
    queryByIdentifierIndex.put(LIST_TYPE, 1);
    collection.createIndex(queryByIdentifierIndex, new IndexOptions().background(true));
  }

  public void findUUIDs(String resourceType, boolean includeSource, OutputStream outputStream) {
    MongoCollection<Document> collection = db.getCollection(resourceType);
    MongoCursor<Document> cursor = getFindUUIDsQuery(collection, includeSource).iterator();

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
