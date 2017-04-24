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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MongoDocumentStoreService {

    private static final String LISTS_COLLECTION = "lists";
    private static final Logger LOG = LoggerFactory.getLogger(MongoDocumentStoreService.class);
    private static final String IDENT_AUTHORITY = "identifiers.authority";
    private static final String IDENT_VALUE = "identifiers.identifierValue";
    private static final String CONCEPT_UUID = "concept.uuid";
    private static final String LIST_TYPE = "listType";

    private final MongoDatabase reader;
    private final MongoDatabase writer;
    
    private final ExecutorService exec = Executors.newCachedThreadPool();
    
    public MongoDocumentStoreService(final MongoDatabase reader, final MongoDatabase writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public Map<String, Object> findByUuid(String resourceType, UUID uuid) {
        try {
            MongoCollection<Document> dbCollection = reader.getCollection(resourceType);
            Document foundDocument = dbCollection.find().filter(Filters.eq("uuid", uuid.toString())).first();
            if (foundDocument == null) {
                throw new DocumentNotFoundException(uuid);
            }

            foundDocument.remove("_id");
            return foundDocument;
        } catch (MongoSocketException | MongoTimeoutException e) {
            LOG.error("MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}, uuids {}", resourceType, uuid, e);
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            LOG.error("Failed to find document in Mongo! Collection {}, uuids {}", resourceType, uuid, e);
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Set<Map<String, Object>> findByUuids(String resourceType, Set<UUID> uuids) {
        try {
            MongoCollection<Document> dbCollection = reader.getCollection(resourceType);

            Iterable<Document> results = dbCollection.find().filter(
                    Filters.in("uuid", uuids.stream().map(UUID::toString).collect(Collectors.toList())));

            Map<UUID, Document> mappedResults = new HashMap<>();
            results.forEach(doc -> mappedResults.put(UUID.fromString((String) doc.get("uuid")), doc));

            // preserve the order of the queried UUIDs in the found documents
            Set<Map<String, Object>> documents = new LinkedHashSet<>();
            uuids.forEach(uuid -> {
                Document doc = mappedResults.get(uuid);
                if (doc != null) {
                    doc.remove("_id");
                    documents.add(doc);
                }
            });

            return documents;
        } catch (MongoSocketException | MongoTimeoutException e) {
            LOG.error("MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}, uuids {}", resourceType, uuids, e);
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            LOG.error("Failed to find document(s) in Mongo! Collection {}, uuids {}", resourceType, uuids, e);
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Map<String, Object> findByIdentifier(String resourceType, String authority, String identifierValue) {
        Bson filter = Filters.and(
                Filters.eq("identifiers.authority", authority),
                Filters.eq("identifiers.identifierValue", identifierValue)
        );

        try {
            MongoCollection<Document> dbCollection = reader.getCollection(resourceType);
            Document found = null;

            for (Document doc : dbCollection.find(filter).limit(2)) {
                if (found == null) {
                    found = doc;
                    found.remove("_id");
                } else {
                    LOG.warn("found too many results for collection {} identifier {}:{}: at least {} and {}",
                            resourceType, authority, identifierValue, found, doc);
                    throw new QueryResultNotUniqueException();
                }
            }

            return found;
        } catch (MongoException e) {
            LOG.error("Failed to find document in Mongo! Collection {}, authority {}, identifierValue {}", resourceType, authority, identifierValue, e);
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Map<String, Object> findByConceptAndType(String resourceType, UUID conceptId, String listType) {
        Bson filter = Filters.and(
                Filters.eq("concept.uuid", conceptId.toString()),
                Filters.eq("listType", listType)
        );

        try {
            MongoCollection<Document> dbCollection = reader.getCollection(resourceType);
            Document found = null;

            for (Document doc : dbCollection.find(filter).limit(2)) {
                if (found == null) {
                    found = doc;
                    found.remove("_id");
                } else {
                    LOG.error("found too many results for collection {} identifier {}:{}: at least {} and {}",
                            resourceType, conceptId, listType, found, doc);
                    return found; // just return the first one we found (graceful degradation) and log the error
                }
            }

            return found;
        } catch (MongoException e) {
            LOG.error("Failed to find document in Mongo! Collection {}, uuid {}, listType {}", resourceType, conceptId, listType, e);
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public void delete(String resourceType, UUID uuid) {
        try {
            MongoCollection<Document> dbCollection = writer.getCollection(resourceType);
            DeleteResult deleteResult = dbCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

            if (deleteResult.getDeletedCount() == 0) {
                throw new DocumentNotFoundException(uuid);
            }

        } catch (MongoSocketException | MongoTimeoutException e) {
            LOG.error("MongoDB connection timed out or caused a socket exception during delete, please check MongoDB! Collection {}, uuid {}", resourceType, uuid, e);
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            LOG.error("Failed to delete document to Mongo! Collection {}, uuid {}", resourceType, uuid, e);
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public DocumentWritten write(String resourceType, Map<String, Object> content) {
        DocumentWritten result;
        
        final String uuid = (String) content.get("uuid");
        final String publishRef = (String)content.get("publishReference");
        try {
            long before = System.currentTimeMillis();
            MongoCollection<Document> dbCollection = writer.getCollection(resourceType);
            Document document = new Document(content);
            UpdateResult updateResult = dbCollection.replaceOne(Filters.eq("uuid", uuid), document, new UpdateOptions().upsert(true));
            if (updateResult.getUpsertedId() == null) {
                result = DocumentWritten.updated(document);
            } else {
                result = DocumentWritten.created(document);
            }
            long after = System.currentTimeMillis();
            LOG.info("written document {} in {} ms", uuid, (after - before));
        } catch (MongoSocketException | MongoTimeoutException e) {
            LOG.error("MongoDB connection timed out or caused a socket exception during write, please check MongoDB! Collection {}, uuid {}", resourceType, content.get("uuid"), e);
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            LOG.error("Failed to write document to Mongo! Collection {}, uuid {}", resourceType, content.get("uuid"), e);
            throw new ExternalSystemInternalServerException(e);
        }
        
        exec.submit(() -> {
          long before = System.currentTimeMillis();
          boolean found = false;
          
          do {
            try {
              Map<String,Object> actual = findByUuid(resourceType, UUID.fromString(uuid));
              found = publishRef.equals(actual.get("publishReference"));
            } catch (DocumentNotFoundException e) {
              try {
                Thread.sleep(50);
              } catch (InterruptedException ex) {/* ignore */}
            }
          } while (!found);
          
          long after = System.currentTimeMillis();
          LOG.info("written document {} could be read after {} ms", uuid, (after - before));
        });
        
        return result;
    }

    @SuppressWarnings("rawtypes")
    public void applyIndexes() {
        applyIndexForCollection("content");
        applyIndexForCollection("internalcomponents");
        applyIndexForListCollection();
    }

    private void applyIndexForListCollection() {
        MongoCollection lists = writer.getCollection(LISTS_COLLECTION);
        LOG.info("Creating UUID index on collection [{}]", LISTS_COLLECTION);
        createUuidIndex(lists);
        LOG.info("Created UUID index on collection [{}]", LISTS_COLLECTION);
        createConceptAndListTypeIndex(lists);
    }

    private void applyIndexForCollection(String collection) {
        MongoCollection mongoCollection = writer.getCollection(collection);
        LOG.info("Creating UUID index on collection [{}]", collection);
        createUuidIndex(mongoCollection);
        LOG.info("Created UUID index on collection [{}]", collection);
        createIdentifierIndex(mongoCollection);
    }

    private void createUuidIndex(MongoCollection<?> collection) {
        collection.createIndex(new Document("uuid", 1), new IndexOptions().background(true).unique(true));
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
        MongoCollection<Document> collection = reader.getCollection(resourceType);
        MongoCursor<Document> cursor = getFindUUIDsQuery(collection, includeSource).iterator();

        try {
            while (cursor.hasNext()){
                Document document = cursor.next();
                outputStream.write((document.toJson() + "\n").getBytes());
            }
            outputStream.flush();
        }catch (IOException e) {
            LOG.error("Error occurred while trying to return ids");
            throw new IDStreamingException(resourceType);
        }
    }

    private FindIterable<Document> getFindUUIDsQuery(MongoCollection<Document> collection, boolean includeSource) {
        List<Bson> projections = new ArrayList<>(Arrays.asList(Projections.include("uuid"), Projections.excludeId()));
        if (includeSource) {
            projections.add(Projections.include(IDENT_AUTHORITY));
        }
        return collection.find().projection(Projections.fields(projections));
    }
}
