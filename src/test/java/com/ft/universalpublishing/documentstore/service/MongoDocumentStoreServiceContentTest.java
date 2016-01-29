package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class MongoDocumentStoreServiceContentTest {
    @ClassRule
    public static final EmbeddedMongoRule MONGO = new EmbeddedMongoRule(12032);
    
    private static final String DB_NAME = "upp-store";
    private static final String DB_COLLECTION = "content";
    private static final String AUTHORITY = "http://junit.example.org/";
    private static final String IDENTIFIER_VALUE = "http://www.example.org/here-is-the-news";

    private Map<String, Object> content;
    private Map<String, Object> outboundContent;

    private MongoDocumentStoreService mongoDocumentStoreService;

    private UUID uuid;
    private Date lastPublicationDate;
    private Date lastModifiedDate;

    private MongoCollection<Document> collection;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        MongoDatabase db = MONGO.client().getDatabase(DB_NAME);
        db.getCollection(DB_COLLECTION).drop();
        
        mongoDocumentStoreService = new MongoDocumentStoreService(db);
        collection = db.getCollection("content");
        uuid = UUID.randomUUID();
        lastPublicationDate = new Date();
        lastModifiedDate = new Date();

        content = new HashMap<>();
        content.put("uuid", uuid.toString());
        content.put("title", "Here is the news");
        content.put("byline", "By Bob Woodward");
        content.put("bodyXML", "xmlBody");
        content.put("publishedDate", lastPublicationDate);
        content.put("publishReference", "Some String");
        content.put("lastModified", lastModifiedDate);

        outboundContent = new HashMap<>();
        outboundContent.put("uuid", uuid.toString());
        outboundContent.put("title", "Here is the news");
        outboundContent.put("byline", "By Bob Woodward");
        outboundContent.put("bodyXML", "xmlBody");
        outboundContent.put("publishedDate", lastPublicationDate);
        outboundContent.put("publishReference", "Some String");
        outboundContent.put("lastModified", lastModifiedDate);
    }

    @Test
    public void contentInStoreShouldBeRetrievedSuccessfully() {
        final Document toInsert = new Document()
                .append("uuid", uuid.toString())
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate);
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
    public void contentShouldBePersistedOnWrite() {
        DocumentWritten result = mongoDocumentStoreService.write("content", content);
        assertThat(result.getMode(), is(Mode.Created));
        Document foundContent = collection.find().filter(Filters.eq("uuid", uuid.toString())).first();
        assertThat(foundContent, notNullValue());
        assertThat((String) foundContent.get("title"), is("Here is the news"));
        assertThat((String) foundContent.get("byline"), is("By Bob Woodward"));
        assertThat((String) foundContent.get("bodyXML"), is("xmlBody"));
        assertThat((Date) foundContent.get("publishedDate"), is(lastPublicationDate));
        assertThat((String)foundContent.get("publishReference"), is("Some String"));
        assertThat((Date) foundContent.get("lastModified"), is(lastModifiedDate));
    }

    @Test
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
    
    @Test
    public void thatFindByIdentifierReturnsDocument()
            throws Exception {
        
        final Document identifier = (new Document())
                .append("authority", AUTHORITY)
                .append("identifierValue", IDENTIFIER_VALUE);
        
        final Document toInsert = (new Document())
                .append("uuid", uuid.toString())
                .append("identifiers", Arrays.asList(identifier))
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate);
        collection.insertOne(toInsert);
        
        Map<String, Object> actual = mongoDocumentStoreService.findByIdentifier(DB_COLLECTION, AUTHORITY, IDENTIFIER_VALUE);
        
        assertThat(actual.get("uuid"), is((Object)uuid.toString()));
    }
    
    @Test
    public void thatFindByIdentifierReturnsNullWhenNotFound()
            throws Exception {
        
        final Document identifier = (new Document())
                .append("authority", AUTHORITY)
                .append("identifierValue", IDENTIFIER_VALUE);
        
        final Document toInsert = (new Document())
                .append("uuid", uuid.toString())
                .append("identifiers", Arrays.asList(identifier))
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate);
        collection.insertOne(toInsert);
        
        Map<String, Object> actual = mongoDocumentStoreService.findByIdentifier(DB_COLLECTION, AUTHORITY, IDENTIFIER_VALUE + "-1");
        
        assertThat(actual, is(nullValue()));
    }
    
    @Test(expected = QueryResultNotUniqueException.class)
    public void thatFindByIdentifierThrowsExceptionOnMultipleMatches()
            throws Exception {
        
        final Document identifier1 = (new Document())
                .append("authority", AUTHORITY)
                .append("identifierValue", IDENTIFIER_VALUE);
        
        final Document doc1 = (new Document())
                .append("uuid", uuid.toString())
                .append("identifiers", Arrays.asList(identifier1))
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate);
        
        final Document identifier2 = (new Document())
                .append("authority", AUTHORITY)
                .append("identifierValue", IDENTIFIER_VALUE);
        
        final Document doc2 = (new Document())
                .append("uuid", uuid.toString())
                .append("identifiers", Arrays.asList(identifier2))
                .append("title", "Here is the news again")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some other String")
                .append("lastModified", lastModifiedDate);
        
        collection.insertMany(Arrays.asList(doc1, doc2));
        
        mongoDocumentStoreService.findByIdentifier(DB_COLLECTION, AUTHORITY, IDENTIFIER_VALUE);
    }
}
