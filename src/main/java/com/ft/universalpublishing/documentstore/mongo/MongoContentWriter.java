package com.ft.universalpublishing.documentstore.mongo;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.ft.content.model.Content;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.write.ContentWritten;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.WriteConcern;

public class MongoContentWriter {
	
    private static final WriteConcern ACKNOWLEDGED = WriteConcern.ACKNOWLEDGED;
	public static final String UUID_QUERY = "{uuid:#}";
	private DBCollection dbCollection;

    public MongoContentWriter(DB db) {
    	dbCollection = db.getCollection("content");
    }

    public ContentWritten write(Content content) {
        try {
        	final JacksonDBCollection<Content, String> coll = JacksonDBCollection.wrap(dbCollection, Content.class,
        	        String.class);
            final String uuid = content.getUuid();

            WriteResult<Content, String> writeResult = coll.update(DBQuery.is("uuid", uuid),
                    content,
                    true,
                    false,
                    WriteConcern.ACKNOWLEDGED);

            return wasUpdate(writeResult) ?
                    ContentWritten.updated(content) :
                    ContentWritten.created(content);
        } catch (MongoSocketException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        } catch (MongoTimeoutException e) {
            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
        }
    }

    private boolean wasUpdate(WriteResult writeResult) {
        return Boolean.TRUE.equals(writeResult.getField("updatedExisting"));
    }

    // TODO - fix this up later
//	public void delete(UUID uuid) {
//		try {
//			final JacksonDBCollection<Content, String> coll = JacksonDBCollection.wrap(dbCollection, Content.class,
//        	        String.class);
//
//            Optional<Content> optionalContent = Optional.fromNullable(contents.findOne(UUID_QUERY, uuid.toString()).as(Content.class));
//            if(!optionalContent.isPresent()){
//            	throw new ContentNotFoundException(uuid);
//            }
//            
//            WriteResult writeResult = contents.remove(UUID_QUERY, uuid.toString());
//
//            if(!wasDeleted(writeResult) ){
//            	throw new RuntimeException(writeResult.getError());
//            }
//        } catch (MongoSocketException e) {
//            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
//        } catch (MongoTimeoutException e) {
//            throw new ExternalSystemUnavailableException("cannot communicate with mongo", e);
//        }
//	}

	// This is a horrible hackish way of knowing that something was deleted - while infact all it is apparently checking that the operation affected atleast one document.
	private boolean wasDeleted(WriteResult writeResult) {
		return writeResult.getN() >  0;
	}


}
