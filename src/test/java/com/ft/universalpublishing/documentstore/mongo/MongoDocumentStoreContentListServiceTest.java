package com.ft.universalpublishing.documentstore.mongo;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.ListItem;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.github.fakemongo.Fongo;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoDocumentStoreContentListServiceTest {
    
    private static final String API_URL_PREFIX_CONTENT = "http://api.ft.com/content/";
    private static final String API_URL_PREFIX_LIST = "http://api.ft.com/lists/";

    private static final String THING_URL_PREFIX = "http://api.ft.com/thing/";

    private static final String DBNAME = "upp-store";

    private static final String WEBURL = "http://www.bbc.co.uk/";
    
    private ContentList inboundContentList;   
    private ContentList outboundContentList;
    private Fongo fongo;
    
    private MongoDocumentStoreService mongoDocumentStoreService;

    private UUID uuid;
    private String contentUuid1;

    private DBCollection collection;
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Before
    public void setup() {
        fongo = new Fongo("embedded");
        DB db = fongo.getDB(DBNAME);
        mongoDocumentStoreService = new MongoDocumentStoreService(db, "api.ft.com");
        collection = db.getCollection("lists");
        uuid = UUID.randomUUID();
        contentUuid1 = UUID.randomUUID().toString();
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(contentUuid1);
        ListItem contentItem2 = new ListItem();
        contentItem2.setWebUrl(WEBURL);
        List<ListItem> inboundItems = ImmutableList.of(contentItem1, contentItem2);
        
        inboundContentList = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(inboundItems)
            .build();
        
        ListItem outboundContentItem1 = new ListItem();
        outboundContentItem1.setId(THING_URL_PREFIX + contentUuid1);
        outboundContentItem1.setApiUrl(API_URL_PREFIX_CONTENT + contentUuid1);
        ListItem outboundContentItem2 = new ListItem();
        outboundContentItem2.setWebUrl(WEBURL);
        List<ListItem> outboundItems = ImmutableList.of(outboundContentItem1, outboundContentItem2);
        
        outboundContentList = new ContentList.Builder()
            .withId(THING_URL_PREFIX + uuid)
            .withApiUrl(API_URL_PREFIX_LIST + uuid)
            .withItems(outboundItems)
            .build();
    }

    
    @Test
    public void contentListInStoreShouldBeRetrievedSuccessfully() {
        BasicDBList items = new BasicDBList();
        items.add(new BasicDBObject().append("uuid", contentUuid1));
        items.add(new BasicDBObject().append("webUrl", WEBURL));
        final BasicDBObject toInsert = new BasicDBObject()
                .append("uuid", uuid.toString())
                .append("items", items); 
        collection.insert(toInsert);
        
        ContentList retrievedContentList = mongoDocumentStoreService.findByUuid("lists", uuid, ContentList.class);
        assertThat(retrievedContentList, is(outboundContentList));
    }
    
    @Test
    public void contentListNotInStoreShouldNotBeReturned() {
        ContentList retrievedContentList = mongoDocumentStoreService.findByUuid("lists", uuid, ContentList.class);
        assertThat(retrievedContentList, nullValue());
    }
    
    @Test
    public void contentListShouldBePersistedOnWrite() {
        DocumentWritten result = mongoDocumentStoreService.write("lists", inboundContentList, ContentList.class);
        assertThat(result.getMode(), is(Mode.Created));
        DBObject findOne = collection.findOne(new BasicDBObject("uuid", uuid.toString()));
        assertThat(findOne , notNullValue());
        
    }
    
    @Test
    public void contentListShouldBeDeletedOnRemove() {
        DocumentWritten result = mongoDocumentStoreService.write("lists", inboundContentList, ContentList.class);
        assertThat(result.getMode(), is(Mode.Created));
        DBObject findOne = collection.findOne(new BasicDBObject("uuid", uuid.toString()));
        assertThat(findOne , notNullValue());
        
        mongoDocumentStoreService.delete("lists", uuid, ContentList.class);
        assertThat(collection.findOne(new BasicDBObject("uuid", uuid.toString())), nullValue());;
    }
    
    @Test
    public void deleteForContentListNotInStoreThrowsContentNotFoundException() {
        exception.expect(DocumentNotFoundException.class);
        exception.expectMessage(String.format("Document with uuid : %s not found!", uuid));

        mongoDocumentStoreService.delete("lists", uuid, ContentList.class);
    }
}
