package com.ft.universalpublishing.documentstore.mongo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.base.Optional;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoDocumentStoreService implements DocumentStoreService {
    
    private static final String IDENTIFIER_TEMPLATE = "http://api.ft.com/thing/";
    private DB db;

    public MongoDocumentStoreService(DB db) {
        this.db = db;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Map<String, Object>> findByUuid(String resourceType, UUID uuid) {
        try {
            DBCollection dbCollection = db.getCollection(resourceType);
            DBObject query = QueryBuilder.start("uuid").is(uuid.toString()).get();
            DBObject result = dbCollection.findOne(query);
            Map<String, Object> resultAsMap = null;
            if (result != null) {
                resultAsMap = result.toMap();
                addId(resultAsMap);
                addRequestUrl(resultAsMap);
                convertToReadFormat(resultAsMap);
                removeMongoId(resultAsMap);
            }   
            return Optional.fromNullable(resultAsMap);
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }

    //TODO work out a way to add id without it being shown last in the json
    private void addId(Map<String, Object> document) {
        String uuid = (String)document.get("uuid");
        document.put("id", IDENTIFIER_TEMPLATE + uuid);
    }

    private void addRequestUrl(Map<String, Object> document) {
        document.put("requestUrl", "TODO - GENERATE REQUEST URL");
    }

    private void removeMongoId(Map<String, Object> document) {
        document.remove("_id");
    }

    private void convertToReadFormat(Map<String, Object> document) {
        //TODO - for each object stored as a value, if it's a map 
        //and has an 'id' field, generate a matching apiUrl
        //ASSUMPTION: all linked stuff is content
        for (Object nested: document.values()) {
            if (nested instanceof List) {
                List<Object> nestedList = (List) nested;
                for (Object obj: nestedList) {
                    if (obj instanceof Map) {
                        Map<String, Object> nestedMap = (Map<String, Object>) obj;
                        String uuid = (String) nestedMap.get("uuid");
                        nestedMap.put("apiUrl", "TODO - GENERATE API URL FOR CONTENT");
                        addId(nestedMap);
                        nestedMap.remove("uuid"); 
                    }
                }
            }
        }
    }

    @Override
    public void delete(String resourceType, UUID uuid) {
        try {

            DBCollection dbCollection = db.getCollection(resourceType);
            DBObject query = QueryBuilder.start("uuid").is(uuid).get();
            DBObject deleted = dbCollection.findAndRemove(query);
            if (deleted == null) {
                throw new ContentNotFoundException(uuid);
            }
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
        
    }

    @Override
    public DocumentWritten write(String resourceType, Document document) {
        try {
            Map<String, Object> documentAsMap = convertToMap(document);
            
            DBCollection dbCollection = db.getCollection(resourceType);
            final String uuid = (String)documentAsMap.get("uuid");
            
            DBObject query = QueryBuilder.start("uuid").is(uuid).get();
            
            com.mongodb.WriteResult writeResult = dbCollection.update(query,
                    new BasicDBObject(documentAsMap),
                    true,
                    false,
                    WriteConcern.ACKNOWLEDGED);

            return wasUpdate(writeResult) ?
                    DocumentWritten.updated(document) :
                    DocumentWritten.created(document);
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }
    
    private boolean wasUpdate(WriteResult writeResult) {
        return writeResult.isUpdateOfExisting();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Document document) {
        ObjectMapper m = new ObjectMapper();
        return m.convertValue(document, Map.class);
    }

}
