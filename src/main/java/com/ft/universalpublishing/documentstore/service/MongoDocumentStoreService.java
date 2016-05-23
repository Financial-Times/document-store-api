package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MongoDocumentStoreService {
    public static final String CONTENT_COLLECTION = "content";
    public static final String LISTS_COLLECTION = "lists";
    
    private static final Logger LOG = LoggerFactory.getLogger(MongoDocumentStoreService.class);
    
    private static final String IDENT_AUTHORITY = "identifiers.authority";
    private static final String IDENT_VALUE = "identifiers.identifierValue";
    
    private final MongoDatabase db;

    public MongoDocumentStoreService(final MongoDatabase db) {
        this.db = db;
    }

    public Map<String, Object> findByUuid(String resourceType, UUID uuid) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document foundDocument = dbCollection.find().filter(Filters.eq("uuid", uuid.toString())).first();
            if (foundDocument == null) {
              throw new DocumentNotFoundException(uuid);
            }
            
            foundDocument.remove("_id");
            return foundDocument;
        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public Set<Map<String, Object>> findByUuids(String resourceType, Set<UUID> uuids) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            
            Iterable<Document> results = dbCollection.find().filter(
                Filters.in("uuid", uuids.stream().map(UUID::toString).collect(Collectors.toList())));
            
            Map<UUID,Document> mappedResults = new HashMap<>();
            results.forEach(doc -> mappedResults.put(UUID.fromString((String)doc.get("uuid")), doc));
            
            // preserve the order of the queried UUIDs in the found documents
            Set<Map<String,Object>> documents = new LinkedHashSet<>();
            uuids.forEach(uuid -> {
              Document doc = mappedResults.get(uuid);
              if (doc != null) {
                doc.remove("_id");
                documents.add(doc);
              }
            });
            
            return documents;
        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }
    
    public Map<String,Object> findByIdentifier(String resourceType, String authority, String identifierValue) {
        Bson filter = Filters.and(
            Filters.eq("identifiers.authority", authority),
            Filters.eq("identifiers.identifierValue", identifierValue)
            );
        
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            Document found = null;
            
            for (Document doc : dbCollection.find(filter).limit(2)) {
                if (found == null) {
                    found = doc;
                    found.remove("_id");
                }
                else {
                    LOG.warn("found too many results for collection {} identifier {}:{}: at least {} and {}",
                            resourceType, authority, identifierValue, found, doc);
                    throw new QueryResultNotUniqueException();
                }
            }
            
            return found;
        }
        catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }
    
    public Map<String, Object> findByConceptAndType(String resourceType, String conceptId, String listType) {
        Bson filter = Filters.and(
                Filters.eq("concept.tmeIdentifier", conceptId),
                Filters.eq("listType", listType)
                );
            
            try {
                MongoCollection<Document> dbCollection = db.getCollection(resourceType);
                Document found = null;
                
                for (Document doc : dbCollection.find(filter).limit(2)) {
                    if (found == null) {
                        found = doc;
                        found.remove("_id");
                    }
                    else {
                        LOG.error("found too many results for collection {} identifier {}:{}: at least {} and {}",
                                resourceType, conceptId, listType, found, doc);
                        return found; // just return the first one we found (graceful degradation) and log the error
                    }
                }
                
                return found;
            }
            catch (MongoException e) {
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
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }

    public DocumentWritten write(String resourceType, Map<String, Object> content) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            final String uuid = (String) content.get("uuid");
            Document document = new Document(content);
            UpdateResult updateResult = dbCollection.replaceOne(Filters.eq("uuid", uuid), document, new UpdateOptions().upsert(true));
            if (updateResult.getUpsertedId() == null) {
                return DocumentWritten.updated(document);
            }
            return DocumentWritten.created(document);
        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoException e) {
            throw new ExternalSystemInternalServerException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public void applyIndexes() {
      MongoCollection content = db.getCollection(CONTENT_COLLECTION);
      createUuidIndex(content);
      createIdentifierIndex(content);
      
      MongoCollection lists = db.getCollection(LISTS_COLLECTION);
      createUuidIndex(lists);
    }
    
    private void createUuidIndex(MongoCollection<?> collection) {
      collection.createIndex(new Document("uuid", 1));
    }
    
    private void createIdentifierIndex(MongoCollection<?> collection) {
      Document queryByIdentifierIndex = new Document();
      queryByIdentifierIndex.put(IDENT_AUTHORITY, 1);
      queryByIdentifierIndex.put(IDENT_VALUE, 1);
      collection.createIndex(queryByIdentifierIndex);
    }

}
