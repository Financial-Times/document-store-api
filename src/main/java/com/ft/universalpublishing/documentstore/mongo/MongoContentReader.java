package com.ft.universalpublishing.documentstore.mongo;

import java.util.UUID;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.content.model.Content;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.reader.ContentWithId;
import com.google.common.base.Optional;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;

public class MongoContentReader {

	private DBCollection dbCollection;

    public MongoContentReader(DB db) {
    	dbCollection = db.getCollection("content");
    }

	public Optional<Content> findByUuid(UUID uuid) {
        try {
			final JacksonDBCollection<ContentWithId, String> coll = JacksonDBCollection.wrap(dbCollection, ContentWithId.class,
        	        String.class);
        	
			Content contentWithId = coll.findOne(DBQuery.is("uuid", uuid.toString()));
        	
            return Optional.fromNullable(contentWithId);
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }

}
