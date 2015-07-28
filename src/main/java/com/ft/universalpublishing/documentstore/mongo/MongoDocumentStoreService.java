package com.ft.universalpublishing.documentstore.mongo;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.Map;
import java.util.UUID;

public class MongoDocumentStoreService implements DocumentStoreService {

    private MongoDatabase db;

    public MongoDocumentStoreService(MongoDatabase db) {
        this.db = db;
    }

    @Override
    public Map<String, Object> findByUuid(String resourceType, UUID uuid) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            return dbCollection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }


    @Override
    public void delete(String resourceType, UUID uuid) {
        try {

            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            DeleteResult deleteResult = dbCollection.deleteOne(Filters.eq("uuid", uuid.toString()));

            if (deleteResult.getDeletedCount() == 0) {
                throw new DocumentNotFoundException(uuid);
            }

        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }

    @Override
    public DocumentWritten write(String resourceType, Map<String, Object> content) {
        try {
            MongoCollection<Document> dbCollection = db.getCollection(resourceType);
            //TODO do we want here this stuff?
//            DBObject uuidIndex = new BasicDBObject("uuid", 1);
//            dbCollection.createIndex(uuidIndex); //creates the index if it doesn't already exist

            final String uuid = (String) content.get("uuid");

            Document document = new Document(content);
            UpdateResult updateResult = dbCollection.replaceOne(Filters.eq("uuid", uuid), document, new UpdateOptions().upsert(true));
            if (updateResult.getUpsertedId() == null) {
                return DocumentWritten.updated(document);
            }

            return DocumentWritten.created(document);

        } catch (MongoSocketException | MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }


}
