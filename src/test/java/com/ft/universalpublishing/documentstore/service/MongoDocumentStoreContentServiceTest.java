package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class MongoDocumentStoreContentServiceTest {

    private static final String DBNAME = "upp-store";

    private Map<String, Object> content;
    private Map<String, Object> outboundContent;

    private MongoDocumentStoreService mongoDocumentStoreService;

    private UUID uuid;
    private Date lastPublicationDate;

    private MongoCollection<Document> collection;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        Fongo fongo = new Fongo("embedded");
        MongoDatabase db = fongo.getDatabase(DBNAME);
        mongoDocumentStoreService = new MongoDocumentStoreService(db);
        collection = db.getCollection("content");
        uuid = UUID.randomUUID();
        lastPublicationDate = new Date();

        content = new HashMap<>();
        content.put("uuid", uuid.toString());
        content.put("title", "Here is the news");
        content.put("byline", "By Bob Woodward");
        content.put("bodyXML", "xmlBody");
        content.put("publishedDate", lastPublicationDate);

        outboundContent = new HashMap<>();
        outboundContent.put("uuid", uuid.toString());
        outboundContent.put("title", "Here is the news");
        outboundContent.put("byline", "By Bob Woodward");
        outboundContent.put("bodyXML", "xmlBody");
        outboundContent.put("publishedDate", lastPublicationDate);
    }


    @Test
    public void contentInStoreShouldBeRetrievedSuccessfully() {
        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate);
        collection.insertOne(toInsert);


        Map<String, Object> contentMap = new HashMap<>(mongoDocumentStoreService.findByUuid("content", uuid));
        contentMap.remove("_id");
        assertThat(contentMap, is(outboundContent));
    }

    @Test
    public void contentNotInStoreShouldNotBeReturned() {
        Map<String, Object> contentMap = mongoDocumentStoreService.findByUuid("content", uuid);
        assertThat(contentMap, nullValue());
    }

    @Test
    @Ignore("Failing due to fongo bug: https://github.com/fakemongo/fongo/issues/118. Please update fongo version when bugfix is released.")
    public void contentShouldBePersistedOnWrite() {
        DocumentWritten result = mongoDocumentStoreService.write("content", content);
        assertThat(result.getMode(), is(Mode.Created));
        Document foundContent = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(foundContent, notNullValue());
        assertThat((String) foundContent.get("title"), is("Here is the news"));
        assertThat((String) foundContent.get("byline"), is("By Bob Woodward"));
        assertThat((String) foundContent.get("bodyXML"), is("xmlBody"));
        assertThat((Date) foundContent.get("publishedDate"), is(lastPublicationDate));

    }

    @Test
    @Ignore("Failing due to fongo bug: https://github.com/fakemongo/fongo/issues/118. Please update fongo version when bugfix is released.")
    public void contentShouldBeDeletedOnRemove() {
        DocumentWritten result = mongoDocumentStoreService.write("content", content);
        assertThat(result.getMode(), is(Mode.Created));
        Document foundContent = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(foundContent, notNullValue());
        assertThat((String) foundContent.get("title"), is("Here is the news"));

        mongoDocumentStoreService.delete("content", uuid);
        assertThat(collection.find().filter(Filters.eq("uuid", uuid.toString())).first(), nullValue());
    }

    @Test
    public void deleteForContentNotInStoreThrowsContentNotFoundException() {
        exception.expect(DocumentNotFoundException.class);
        exception.expectMessage(String.format("Document with uuid : %s not found!", uuid));

        mongoDocumentStoreService.delete("content", uuid);
    }
}
