package com.ft.universalpublishing.documentstore.service;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
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
    private static final Document METHODE_AUTHORITY = new Document().append("authority", "http://api.ft.com/system/FTCOM-METHODE");
    private static final Document WORDPRESS_AUTHORITY = new Document().append("authority", "http://api.ft.com/system/FT-LABS-WP-1-335");

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
        mongoDocumentStoreService.applyIndexes();
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

    @Test(expected = DocumentNotFoundException.class)
    public void contentNotInStoreShouldNotBeReturned() {
        mongoDocumentStoreService.findByUuid("content", uuid);
    }

    @Test
    public void thatFindByUuidsReturnsAllAndOnlyMatches() {
        final Document doc1 = new Document()
                .append("uuid", uuid.toString())
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate);
        
        UUID uuid2 = UUID.randomUUID();
        final Document doc2 = new Document()
            .append("uuid", uuid2.toString())
            .append("title", "Here is the news again")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some other String")
            .append("lastModified", lastModifiedDate);
        
        UUID uuid3 = UUID.randomUUID();
        final Document doc3 = new Document()
            .append("uuid", uuid3.toString())
            .append("title", "Here is the news yet again")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Yet another String")
            .append("lastModified", lastModifiedDate);

        collection.insertMany(Arrays.asList(doc1, doc2, doc3));
        
        Set<UUID> uuids = new LinkedHashSet<>();
        uuids.add(uuid);
        uuids.add(uuid2);
        
        Set<Map<String, Object>> content = mongoDocumentStoreService.findByUuids("content", uuids);
        
        assertThat(content.size(), equalTo(2));
        
        List<String> actualUuids = content.stream().map(m -> (String)m.get("uuid")).collect(Collectors.toList());
        assertThat(actualUuids, contains(uuid.toString(), uuid2.toString()));
        
        assertThat(content.stream().filter(m -> m.containsKey("_id")).findAny().isPresent(), equalTo(false));
    }

    @Test
    public void thatFindByUuidsReturnsSubsetOfMatches() {
        final Document doc1 = new Document()
                .append("uuid", uuid.toString())
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate);
        
        UUID uuid2 = UUID.randomUUID();

        collection.insertOne(doc1);
        
        Set<UUID> uuids = new LinkedHashSet<>();
        uuids.add(uuid2);
        uuids.add(uuid);
        
        Set<Map<String, Object>> content = mongoDocumentStoreService.findByUuids("content", uuids);
        
        assertThat(content.size(), equalTo(1));
        assertThat((String)content.iterator().next().get("uuid"), equalTo(uuid.toString()));
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
        assertThat((String) foundContent.get("publishReference"), is("Some String"));
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

        assertThat(actual.get("uuid"), is((Object) uuid.toString()));
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

    @Test(expected = MongoBulkWriteException.class) // throws bulk write because of the unique constraint on the index.
    public void thatFindByIdentifierThrowsExceptionOnMultipleMatches() throws Exception {
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

    @Test
    public void thatIndexesAreConfigured() {
        Supplier<Stream<Document>> indexes = () -> StreamSupport.stream(collection.listIndexes().spliterator(), false);

        Document uuidKey = new Document("uuid", 1);
        assertThat("UUID index", indexes.get().anyMatch(doc -> uuidKey.equals(doc.get("key")) && doc.getBoolean("unique")), is(true));

        Document identifierKey = new Document();
        identifierKey.put("identifiers.authority", 1);
        identifierKey.put("identifiers.identifierValue", 1);
        assertThat("Identifiers index", indexes.get().anyMatch(doc -> identifierKey.equals(doc.get("key"))), is(true));
    }

    @Test
    public void idsContentShouldBeRetrievedSuccessfully() throws IOException {
        final String firstUUID = "d08ef814-f295-11e6-a94b-0e7d0412f5a5";
        final Document firstDocument = new Document()
                .append("uuid", firstUUID)
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate)
                .append("identifiers", WORDPRESS_AUTHORITY);
        collection.insertOne(firstDocument);

        final String secondUUID = "8ae3f1dc-f288-11e6-8758-6876151821a6";
        final Document secondDocument = new Document()
                .append("uuid", secondUUID)
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate)
                .append("identifiers", METHODE_AUTHORITY);
        collection.insertOne(secondDocument);

        OutputStream expected = new ByteArrayOutputStream();
        OutputStream actual = new ByteArrayOutputStream();
        expected.write((new Document("uuid", firstUUID).toJson() + "\n").getBytes());
        expected.write((new Document("uuid", secondUUID).toJson() + "\n").getBytes());
        mongoDocumentStoreService.findUUIDs("content", Boolean.FALSE, actual);
        assertThat(actual.toString(), equalTo("{ \"uuid\" : \"d08ef814-f295-11e6-a94b-0e7d0412f5a5\" }\n" +
                "{ \"uuid\" : \"8ae3f1dc-f288-11e6-8758-6876151821a6\" }\n"));
    }

    @Test
    public void idsAndSourceOfForContentShouldBeRetrievedSuccessfully() throws IOException {
        final String firstUUID = "d08ef814-f295-11e6-a94b-0e7d0412f5a5";
        final Document firstDocument = new Document()
                .append("uuid", firstUUID)
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate)
                .append("identifiers", WORDPRESS_AUTHORITY);
        collection.insertOne(firstDocument);

        final String secondUUID = "8ae3f1dc-f288-11e6-8758-6876151821a6";
        final Document secondDocument = new Document()
                .append("uuid", secondUUID)
                .append("title", "Here is the news")
                .append("byline", "By Bob Woodward")
                .append("bodyXML", "xmlBody")
                .append("publishedDate", lastPublicationDate)
                .append("publishReference", "Some String")
                .append("lastModified", lastModifiedDate)
                .append("identifiers", METHODE_AUTHORITY);
        collection.insertOne(secondDocument);

        OutputStream expected = new ByteArrayOutputStream();
        OutputStream actual = new ByteArrayOutputStream();
        expected.write((new Document("uuid", secondUUID).toJson() + "\n").getBytes());
        mongoDocumentStoreService.findUUIDs("content", Boolean.TRUE, actual);
        assertThat(actual.toString(), equalTo("{ \"uuid\" : \"d08ef814-f295-11e6-a94b-0e7d0412f5a5\", \"identifiers\" : { \"authority\" : \"http://api.ft.com/system/FT-LABS-WP-1-335\" } }\n" +
                "{ \"uuid\" : \"8ae3f1dc-f288-11e6-8758-6876151821a6\", \"identifiers\" : { \"authority\" : \"http://api.ft.com/system/FTCOM-METHODE\" } }\n"));
    }
}
