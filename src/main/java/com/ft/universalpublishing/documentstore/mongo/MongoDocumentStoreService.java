package com.ft.universalpublishing.documentstore.mongo;

import java.util.UUID;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.WriteConcern;

public class MongoDocumentStoreService implements DocumentStoreService {

    private DB db;
    private String apiPath;

    public MongoDocumentStoreService(DB db, String apiPath) {
        this.db = db;
        this.apiPath = apiPath;
    }

    @Override
    public <T extends Document> T findByUuid(String resourceType, UUID uuid, Class<T> documentClass) {
        try {
            DBCollection dbCollection = db.getCollection(resourceType);
            
            final JacksonDBCollection<T, String> coll = JacksonDBCollection.wrap(dbCollection, documentClass,
                    String.class);
            
            T result = coll.findOne(DBQuery.is("uuid", uuid.toString())); 
            
            if (result != null) {
                result.addIds();
                result.addApiUrls(apiPath);
                result.removePrivateFields();
            }
 
            return result;
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }


    @Override
    public <T extends Document> void delete(String resourceType, UUID uuid, Class<T> documentClass) {
        try {
            
            DBCollection dbCollection = db.getCollection(resourceType);
            
            final JacksonDBCollection<T, String> coll = JacksonDBCollection.wrap(dbCollection, documentClass,
                    String.class);
            
            WriteResult<T, String> result = coll.remove(DBQuery.is("uuid", uuid.toString()));

            if (!wasDelete(result)) {
                throw new DocumentNotFoundException(uuid);
            }
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
        
    }

    @Override
    public <T extends Document> DocumentWritten write(String resourceType, T document, Class<T> documentClass) {
        try {
            DBObject uuidIndex = new BasicDBObject("uuid", 1);
            DBCollection dbCollection = db.getCollection(resourceType);
            
            dbCollection.createIndex(uuidIndex); //creates the index if it doesn't already exist
            
            final JacksonDBCollection<T, String> coll = JacksonDBCollection.wrap(dbCollection, documentClass,
                    String.class);
            
            final String uuid = document.getUuid();

            WriteResult<T, String> writeResult = coll.update(DBQuery.is("uuid", uuid),
                    document,
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
    
    private <T extends Document> boolean wasUpdate(WriteResult<T, String> writeResult) {
        return writeResult.getWriteResult().isUpdateOfExisting();
    }
    
    private <T extends Document> boolean wasDelete(WriteResult<T, String> writeResult) {
        return writeResult.getN() > 0? true: false;
    }

}
