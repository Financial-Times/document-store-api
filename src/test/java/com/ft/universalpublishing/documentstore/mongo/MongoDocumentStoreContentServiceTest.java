package com.ft.universalpublishing.documentstore.mongo;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoDocumentStoreContentServiceTest {

//    private static final String API_URL_PREFIX_CONTENT = "http://api.ft.com/content/";
//
//    private static final String THING_URL_PREFIX = "http://api.ft.com/thing/";
//
//    private static final String DBNAME = "upp-store";
//
//    private Content content;
//    private Content outboundContent;
//    private Fongo fongo;
//
//    private MongoDocumentStoreService mongoDocumentStoreService;
//
//    private UUID uuid;
//    private Date lastPublicationDate;
//
//    private DBCollection collection;
//
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();
//
//    @Before
//    public void setup() {
//        fongo = new Fongo("embedded");
//        DB db = fongo.getDB(DBNAME);
//        mongoDocumentStoreService = new MongoDocumentStoreService(db, "api.ft.com");
//        collection = db.getCollection("content");
//        uuid = UUID.randomUUID();
//        lastPublicationDate = new Date();
//        content = new Content();
//        content.setUuid(uuid.toString());
//        content.setTitle("Here is the news");
//        content.setByline("By Bob Woodward");
//        content.setBodyXML("xmlBody");
//        content.setPublishedDate(lastPublicationDate);
//        collection = db.getCollection("content");
//
//        outboundContent = new Content();
//        outboundContent.setId(THING_URL_PREFIX + uuid);
//        outboundContent.setTitle("Here is the news");
//        outboundContent.setByline("By Bob Woodward");
//        outboundContent.setBodyXML("xmlBody");
//        outboundContent.setPublishedDate(lastPublicationDate);
//        outboundContent.setRequestUrl(API_URL_PREFIX_CONTENT + uuid);
//    }
//
//
//    @Test
//    public void contentInStoreShouldBeRetrievedSuccessfully() {
//        final BasicDBObject toInsert = new BasicDBObject()
//                .append("uuid", uuid.toString())
//                .append("title", "Here is the news")
//                .append("byline", "By Bob Woodward")
//                .append("bodyXML", "xmlBody")
//                .append("publishedDate", lastPublicationDate);
//        collection.insert(toInsert);
//
//
//        Content retrievedContent = mongoDocumentStoreService.findByUuid("content", uuid, Content.class);
//        assertThat(retrievedContent, is(outboundContent));
//    }
//
//    @Test
//    public void contentNotInStoreShouldNotBeReturned() {
//        Content retrievedContent = mongoDocumentStoreService.findByUuid("content", uuid, Content.class);
//        assertThat(retrievedContent, nullValue());
//    }
//
//    @Test
//    public void contentShouldBePersistedOnWrite() {
//        DocumentWritten result = mongoDocumentStoreService.write("content", content, Content.class);
//        assertThat(result.getMode(), is(Mode.Created));
//        DBObject findOne = collection.findOne(new BasicDBObject("uuid", uuid.toString()));
//        assertThat(findOne , notNullValue());
//        assertThat((String)findOne.get("title"), is("Here is the news"));
//        assertThat((String)findOne.get("byline"), is("By Bob Woodward"));
//        assertThat((String)findOne.get("bodyXML"), is("xmlBody"));
//        assertThat((Date)findOne.get("publishedDate"), is(lastPublicationDate));
//
//    }
//
//    @Test
//    public void contentShouldBeDeletedOnRemove() {
//        DocumentWritten result = mongoDocumentStoreService.write("content", content, Content.class);
//        assertThat(result.getMode(), is(Mode.Created));
//        DBObject findOne = collection.findOne(new BasicDBObject("uuid", uuid.toString()));
//        assertThat(findOne , notNullValue());
//        assertThat((String)findOne.get("title"), is("Here is the news"));
//
//        mongoDocumentStoreService.delete("content", uuid, Content.class);
//        assertThat(collection.findOne(new BasicDBObject("uuid", uuid.toString())), nullValue());;
//    }
//
//    @Test
//    public void deleteForContentNotInStoreThrowsContentNotFoundException() {
//        exception.expect(DocumentNotFoundException.class);
//        exception.expectMessage(String.format("Document with uuid : %s not found!", uuid));
//
//        mongoDocumentStoreService.delete("content", uuid, Content.class);
//    }
}
