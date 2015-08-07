package com.ft.universalpublishing.documentstore.service;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Before;
import org.junit.Ignore;
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

public class MongoDocumentStoreContentListServiceTest {
    
    private static final String DBNAME = "upp-store";
    private static final String WEBURL = "http://www.bbc.co.uk/";

    private MongoDocumentStoreService mongoDocumentStoreService;

    private UUID uuid;
    private String contentUuid1;
    private MongoCollection<Document> collection;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        Fongo fongo = new Fongo("embedded");
        MongoDatabase db = fongo.getDatabase(DBNAME);
        mongoDocumentStoreService = new MongoDocumentStoreService(db);//, "api.ft.com");
        collection = db.getCollection("lists");
        uuid = UUID.randomUUID();
        contentUuid1 = UUID.randomUUID().toString();
    }

    private List<ListItem> mockInboundListItems() {
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(contentUuid1);
        ListItem contentItem2 = new ListItem();
        contentItem2.setWebUrl(WEBURL);
        return ImmutableList.of(contentItem1, contentItem2);
    }

    private List<ListItem> mockOutboundListItems() {
        ListItem outboundContentItem1 = new ListItem();
        outboundContentItem1.setUuid(contentUuid1);
        ListItem outboundContentItem2 = new ListItem();
        outboundContentItem2.setWebUrl(WEBURL);
        return ImmutableList.of(outboundContentItem1, outboundContentItem2);
    }

    @Test
    public void contentListInStoreShouldBeRetrievedSuccessfully() {
        BasicDBList items = new BasicDBList();
        items.add(new BasicDBObject().append("uuid", contentUuid1));
        items.add(new BasicDBObject().append("webUrl", WEBURL));
        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("items", items);
        collection.insertOne(toInsert);

        ContentList expectedList = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockOutboundListItems())
            .build();

        Map<String,Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        ContentList retrievedContentList = new ObjectMapper().convertValue(contentMap, ContentList.class);

        assertThat(retrievedContentList, is(expectedList));
    }

    @Test
    public void contentListNotInStoreShouldNotBeReturned() {
        Map<String, Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        assertThat(contentMap, nullValue());
    }

    @Test
    @Ignore("Failing due to fongo bug: https://github.com/fakemongo/fongo/issues/118. Please update fongo version when bugfix is released.")
    public void contentListShouldBePersistedOnWrite() {
        ContentList list = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockInboundListItems())
            .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));

        Document findOne = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(findOne , notNullValue());
    }

    @Test
    @Ignore("Failing due to fongo bug: https://github.com/fakemongo/fongo/issues/118. Please update fongo version when bugfix is released.")
    public void thatLayoutHintIsPersisted() {
        String hint = "junit-layout";
        ContentList list = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockInboundListItems())
            .withLayoutHint(hint)
            .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));

        ContentList actual = new ObjectMapper().convertValue(result.getDocument(), ContentList.class);
        assertThat("list uuid", actual.getUuid(), is(uuid.toString()));
        assertThat("layout hint", actual.getLayoutHint(), is(hint));
    }

    @Test
    public void thatLayoutHintIsRetrieved() {
        String hint = "junit-layout";

        BasicDBList items = new BasicDBList();
        items.add(new BasicDBObject().append("uuid", contentUuid1));
        items.add(new BasicDBObject().append("webUrl", WEBURL));

        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("layoutHint", hint)
                .append("items", items);

        collection.insertOne(toInsert);

        ContentList expectedList = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockOutboundListItems())
            .withLayoutHint(hint)
            .build();

        Map<String, Object> contentMap = mongoDocumentStoreService.findByUuid("lists", uuid);
        ContentList retrievedContentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
        assertThat(retrievedContentList, is(expectedList));
    }

    @Test
    @Ignore("Failing due to fongo bug: https://github.com/fakemongo/fongo/issues/118. Please update fongo version when bugfix is released.")
    public void contentListShouldBeDeletedOnRemove() {
        ContentList list = new ContentList.Builder()
            .withUuid(uuid)
            .withItems(mockInboundListItems())
            .build();

        DocumentWritten result = mongoDocumentStoreService.write("lists", new ObjectMapper().convertValue(list, Map.class));
        assertThat(result.getMode(), is(Mode.Created));
        Document findOne = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(findOne , notNullValue());

        mongoDocumentStoreService.delete("lists", uuid);
        assertThat(collection.find().filter(Filters.eq("uuid", uuid.toString())).first(), nullValue());
    }

    @Test
    public void deleteForContentListNotInStoreThrowsContentNotFoundException() {
        exception.expect(DocumentNotFoundException.class);
        exception.expectMessage(String.format("Document with uuid : %s not found!", uuid));

        mongoDocumentStoreService.delete("lists", uuid);
    }
}
