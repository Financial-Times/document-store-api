package com.ft.universalpublishing.documentstore.service;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.ft.universalpublishing.documentstore.write.DocumentWritten.Mode;
import com.google.common.collect.ImmutableMap;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MongoDocumentStoreServiceContentTest {

  private static final String DB_NAME = "upp-store";
  private static final String DB_COLLECTION = "content";
  private static final String AUTHORITY = "http://junit.example.org/";
  private static final String IDENTIFIER_VALUE = "http://www.example.org/here-is-the-news";
  private static final Document METHODE_AUTHORITY =
      new Document().append("authority", "http://api.ft.com/system/FTCOM-METHODE");
  private static final Document WORDPRESS_AUTHORITY =
      new Document().append("authority", "http://api.ft.com/system/FT-LABS-WP-1-335");

  @RegisterExtension
  static EmbeddedMongoExtension mongo =
      EmbeddedMongoExtension.builder().dbName(DB_NAME).dbCollection(DB_COLLECTION).build();

  private Map<String, Object> content;
  private Map<String, Object> outboundContent;

  private MongoDocumentStoreService mongoDocumentStoreService;

  private UUID uuid;
  private Date lastPublicationDate;
  private Date lastModifiedDate;

  private MongoCollection<Document> collection;

  @BeforeEach
  public void prepareDbData() {
    MongoDatabase db = mongo.getDb();
    mongoDocumentStoreService =
        new MongoDocumentStoreService(db, Executors.newSingleThreadExecutor());
    mongoDocumentStoreService.applyIndexes();
    collection = db.getCollection(DB_COLLECTION);
    uuid = UUID.randomUUID();
    lastPublicationDate = new Date();
    lastModifiedDate = new Date();

    content =
        ImmutableMap.<String, Object>builder()
            .put("uuid", uuid.toString())
            .put("title", "Here is the news")
            .put("byline", "By Bob Woodward")
            .put("bodyXML", "xmlBody")
            .put("publishedDate", lastPublicationDate)
            .put("publishReference", "Some String")
            .put("lastModified", lastModifiedDate)
            .build();

    outboundContent =
        ImmutableMap.<String, Object>builder()
            .put("uuid", uuid.toString())
            .put("title", "Here is the news")
            .put("byline", "By Bob Woodward")
            .put("bodyXML", "xmlBody")
            .put("publishedDate", lastPublicationDate)
            .put("publishReference", "Some String")
            .put("lastModified", lastModifiedDate)
            .build();
  }

  @Test
  public void contentInStoreShouldBeRetrievedSuccessfully() {
    final Document toInsert =
        new Document()
            .append("uuid", uuid.toString())
            .append("title", "Here is the news")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some String")
            .append("lastModified", lastModifiedDate);
    collection.insertOne(toInsert);

    Map<String, Object> contentMap =
        new HashMap<>(mongoDocumentStoreService.findByUuid("content", uuid));
    contentMap.remove("_id");
    assertThat(contentMap, is(outboundContent));
  }

  @Test
  public void contentNotInStoreShouldNotBeReturned() {
    assertThrows(
        DocumentNotFoundException.class,
        () -> mongoDocumentStoreService.findByUuid("content", uuid));
  }

  @Test
  public void thatFindByUuidsReturnsAllAndOnlyMatches() {
    final Document doc1 =
        new Document()
            .append("uuid", uuid.toString())
            .append("title", "Here is the news")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some String")
            .append("lastModified", lastModifiedDate);

    UUID uuid2 = UUID.randomUUID();
    final Document doc2 =
        new Document()
            .append("uuid", uuid2.toString())
            .append("title", "Here is the news again")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some other String")
            .append("lastModified", lastModifiedDate);

    UUID uuid3 = UUID.randomUUID();
    final Document doc3 =
        new Document()
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

    List<Document> content = mongoDocumentStoreService.findByUuids("content", uuids);

    assertThat(content.size(), equalTo(2));

    List<String> actualUuids =
        content.stream().map(m -> (String) m.get("uuid")).collect(Collectors.toList());
    assertThat(actualUuids, contains(uuid.toString(), uuid2.toString()));

    assertThat(content.stream().anyMatch(m -> m.containsKey("_id")), equalTo(false));
  }

  @Test
  public void thatFindByUuidsReturnsSubsetOfMatches() {
    final Document doc1 =
        new Document()
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

    List<Document> content = mongoDocumentStoreService.findByUuids("content", uuids);

    assertThat(content.size(), equalTo(1));
    assertThat((String) content.iterator().next().get("uuid"), equalTo(uuid.toString()));
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
    Exception exception =
        assertThrows(
            DocumentNotFoundException.class,
            () -> mongoDocumentStoreService.delete("content", uuid));

    String expectedMessage = String.format("Document with uuid : %s not found!", uuid);
    assertThat(exception.getMessage(), equalTo(expectedMessage));
  }

  @Test
  public void thatFindByIdentifierReturnsDocument() {
    final Document identifier =
        (new Document()).append("authority", AUTHORITY).append("identifierValue", IDENTIFIER_VALUE);

    final Document toInsert =
        (new Document())
            .append("uuid", uuid.toString())
            .append("identifiers", Collections.singletonList(identifier))
            .append("title", "Here is the news")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some String")
            .append("lastModified", lastModifiedDate);
    collection.insertOne(toInsert);

    Map<String, Object> actual =
        mongoDocumentStoreService.findByIdentifier(DB_COLLECTION, AUTHORITY, IDENTIFIER_VALUE);

    assertThat(actual.get("uuid"), is((Object) uuid.toString()));
  }

  @Test
  public void thatFindByIdentifierReturnsNullWhenNotFound() {
    final Document identifier =
        (new Document()).append("authority", AUTHORITY).append("identifierValue", IDENTIFIER_VALUE);

    final Document toInsert =
        (new Document())
            .append("uuid", uuid.toString())
            .append("identifiers", Collections.singletonList(identifier))
            .append("title", "Here is the news")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some String")
            .append("lastModified", lastModifiedDate);
    collection.insertOne(toInsert);

    Map<String, Object> actual =
        mongoDocumentStoreService.findByIdentifier(
            DB_COLLECTION, AUTHORITY, IDENTIFIER_VALUE + "-1");

    assertThat(actual, is(nullValue()));
  }

  @Test
  public void thatFindByIdentifierThrowsExceptionOnMultipleMatches() {
    final Document identifier1 =
        new Document().append("authority", AUTHORITY).append("identifierValue", IDENTIFIER_VALUE);

    final Document doc1 =
        new Document()
            .append("uuid", uuid.toString())
            .append("identifiers", Collections.singletonList(identifier1))
            .append("title", "Here is the news")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some String")
            .append("lastModified", lastModifiedDate);

    final Document identifier2 =
        new Document().append("authority", AUTHORITY).append("identifierValue", IDENTIFIER_VALUE);

    final Document doc2 =
        new Document()
            .append("uuid", uuid.toString())
            .append("identifiers", Collections.singletonList(identifier2))
            .append("title", "Here is the news again")
            .append("byline", "By Bob Woodward")
            .append("bodyXML", "xmlBody")
            .append("publishedDate", lastPublicationDate)
            .append("publishReference", "Some other String")
            .append("lastModified", lastModifiedDate);

    // throws bulk write because of the unique constraint on the index.
    assertThrows(
        MongoBulkWriteException.class,
        () -> {
          collection.insertMany(Arrays.asList(doc1, doc2));
          mongoDocumentStoreService.findByIdentifier(DB_COLLECTION, AUTHORITY, IDENTIFIER_VALUE);
        });
  }

  @Test
  public void thatIndexesAreConfigured() {
    Supplier<Stream<Document>> indexes =
        () -> StreamSupport.stream(collection.listIndexes().spliterator(), false);

    Document uuidKey = new Document("uuid", 1);
    assertThat(
        "UUID index",
        indexes.get().anyMatch(doc -> uuidKey.equals(doc.get("key")) && doc.getBoolean("unique")),
        is(true));

    Document identifierKey = new Document();
    identifierKey.put("identifiers.authority", 1);
    identifierKey.put("identifiers.identifierValue", 1);
    assertThat(
        "Identifiers index",
        indexes.get().anyMatch(doc -> identifierKey.equals(doc.get("key"))),
        is(true));
  }

  @Test
  public void idsContentShouldBeRetrievedSuccessfully() throws IOException {
    final String firstUUID = "d08ef814-f295-11e6-a94b-0e7d0412f5a5";
    final Document firstDocument =
        new Document()
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
    final Document secondDocument =
        new Document()
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
    assertThat(
        actual.toString(),
        equalTo(
            "{ \"uuid\" : \"d08ef814-f295-11e6-a94b-0e7d0412f5a5\" }\n"
                + "{ \"uuid\" : \"8ae3f1dc-f288-11e6-8758-6876151821a6\" }\n"));
  }

  @Test
  public void idsAndSourceOfForContentShouldBeRetrievedSuccessfully() throws IOException {
    final String firstUUID = "d08ef814-f295-11e6-a94b-0e7d0412f5a5";
    final Document firstDocument =
        new Document()
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
    final Document secondDocument =
        new Document()
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
    assertThat(
        actual.toString(),
        equalTo(
            "{ \"uuid\" : \"d08ef814-f295-11e6-a94b-0e7d0412f5a5\", \"identifiers\" : { \"authority\" : \"http://api.ft.com/system/FT-LABS-WP-1-335\" } }\n"
                + "{ \"uuid\" : \"8ae3f1dc-f288-11e6-8758-6876151821a6\", \"identifiers\" : { \"authority\" : \"http://api.ft.com/system/FTCOM-METHODE\" } }\n"));
  }
}
