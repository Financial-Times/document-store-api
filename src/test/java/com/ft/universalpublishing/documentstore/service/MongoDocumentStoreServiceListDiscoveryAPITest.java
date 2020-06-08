package com.ft.universalpublishing.documentstore.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.bson.Document;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MongoDocumentStoreServiceListDiscoveryAPITest {
  private static final String DB_NAME = "upp-store";
  private static final String COLLECTION_NAME = "collection-name";

  private static final Map<String, Object> TEST_DATA_All =
      ImmutableMap.<String, Object>builder()
          .put("uuid", UUID.randomUUID())
          .put("conceptUUID", UUID.randomUUID())
          .put("conceptPrefLabel", "Concept MatchAll")
          .put("title", "Title MatchAll")
          .put("listType", "MatchAll")
          .put("webUrl", "http://www.ft.com/ig/sites/2014/virgingroup-timeline/")
          .put("standfirst", "some standfirst")
          .build();

  private static final Map<String, Object> TEST_DATA_CONCEPT =
      ImmutableMap.<String, Object>builder()
          .put("uuid", UUID.randomUUID())
          .put("conceptUUID", UUID.randomUUID())
          .put("conceptPrefLabel", "Concept LookFor")
          .put("title", "Title Test")
          .put("listType", "Test")
          .build();

  private static final Map<String, Object> TEST_DATA_TITLE =
      ImmutableMap.<String, Object>builder()
          .put("uuid", UUID.randomUUID())
          .put("conceptUUID", UUID.randomUUID())
          .put("conceptPrefLabel", "Concept Test")
          .put("title", "Title LookFor")
          .put("listType", "Test")
          .build();

  private static final Map<String, Object> TEST_DATA_LIST_TYPE =
      ImmutableMap.<String, Object>builder()
          .put("uuid", UUID.randomUUID())
          .put("conceptUUID", UUID.randomUUID())
          .put("conceptPrefLabel", "Concept Test")
          .put("title", "Title Test")
          .put("listType", "LookFor")
          .build();

  private static final Document TEST_CONCEPT0 =
      new Document()
          .append("uuid", TEST_DATA_All.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_All.get("conceptPrefLabel"));

  private static final Document ENTRY0 =
      new Document()
          .append("uuid", TEST_DATA_All.get("uuid"))
          .append("title", TEST_DATA_All.get("title"))
          .append("concept", TEST_CONCEPT0)
          .append("listType", TEST_DATA_All.get("listType"))
          .append("webUrl", TEST_DATA_All.get("webUrl"))
          .append("standfirst", TEST_DATA_All.get("standfirst"));

  private static final Document TEST_CONCEPT1 =
      new Document()
          .append("uuid", TEST_DATA_CONCEPT.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_CONCEPT.get("conceptPrefLabel"));

  private static final Document ENTRY1 =
      new Document()
          .append("uuid", TEST_DATA_CONCEPT.get("uuid"))
          .append("title", TEST_DATA_CONCEPT.get("title"))
          .append("concept", TEST_CONCEPT1)
          .append("listType", TEST_DATA_CONCEPT.get("listType"))
          .append("webUrl", TEST_DATA_All.get("webUrl"))
          .append("standfirst", "random");

  private static final Document TEST_CONCEPT2 =
      new Document()
          .append("uuid", TEST_DATA_TITLE.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_TITLE.get("conceptPrefLabel"));

  private static final Document ENTRY2 =
      new Document()
          .append("uuid", TEST_DATA_TITLE.get("uuid"))
          .append("title", TEST_DATA_TITLE.get("title"))
          .append("concept", TEST_CONCEPT2)
          .append("listType", TEST_DATA_TITLE.get("listType"));

  private static final Document TEST_CONCEPT3 =
      new Document()
          .append("uuid", TEST_DATA_LIST_TYPE.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_LIST_TYPE.get("conceptPrefLabel"));

  private static final Document ENTRY3 =
      new Document()
          .append("uuid", TEST_DATA_LIST_TYPE.get("uuid"))
          .append("title", TEST_DATA_LIST_TYPE.get("title"))
          .append("concept", TEST_CONCEPT3)
          .append("listType", TEST_DATA_LIST_TYPE.get("listType"));

  private static final List<Document> TEST_DATA = Arrays.asList(ENTRY0, ENTRY1, ENTRY2, ENTRY3);

  @RegisterExtension
  static EmbeddedMongoExtension mongo =
      EmbeddedMongoExtension.builder().dbName(DB_NAME).dbCollection(COLLECTION_NAME).build();

  private MongoDocumentStoreService mongoDocumentStoreService;
  private MongoCollection<Document> collection;

  @BeforeEach
  public void setup() {
    MongoDatabase db = mongo.getDb();
    mongoDocumentStoreService =
        new MongoDocumentStoreService(db, Executors.newSingleThreadExecutor());
    mongoDocumentStoreService.applyIndexes();
    collection = db.getCollection(COLLECTION_NAME);
  }

  @Test
  public void searchWithNoParamsAndNoEntriesShouldReturnEmptyArray() {
    UUID[] tConceptUUID = new UUID[] {};

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUID, null, null, null, null);

    assertThat(filteredEntries.size(), is(0));
  }

  @Test
  public void searchWithNoParamsShouldReturnAllEntries() {
    collection.insertMany(TEST_DATA);

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(COLLECTION_NAME, null, null, null, null, null);

    assertThat(filteredEntries.size(), is(TEST_DATA.size()));
  }

  @Test
  public void searchByConceptUUIDReturnsAllEntriesWithThisConcept() {
    collection.insertMany(TEST_DATA);
    UUID[] tConceptUUIDs =
        new UUID[] {UUID.fromString(TEST_DATA_CONCEPT.get("conceptUUID").toString())};

    Concept concept =
        new Concept(
            (UUID) TEST_DATA_CONCEPT.get("conceptUUID"),
            (String) TEST_DATA_CONCEPT.get("conceptPrefLabel"));

    ContentList expectedEntries =
        new ContentList.Builder()
            .withUuid((UUID) TEST_DATA_CONCEPT.get("uuid"))
            .withTitle((String) TEST_DATA_CONCEPT.get("title"))
            .withConcept(concept)
            .withListType((String) TEST_DATA_CONCEPT.get("listType"))
            .build();

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, null, null, null, null);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredEntries.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedEntries));
  }

  @Test
  public void searchByConceptUUIDReturnsAllListsForMultipleConcepts() {
    collection.insertMany(TEST_DATA);
    UUID[] tConceptUUIDs =
        new UUID[] {
          UUID.fromString(TEST_DATA_CONCEPT.get("conceptUUID").toString()),
          UUID.fromString(TEST_DATA_TITLE.get("conceptUUID").toString())
        };

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, null, null, null, null);
    List<UUID> conceptUUIDsList = Arrays.asList(tConceptUUIDs);

    assertThat(filteredEntries.size(), is(conceptUUIDsList.size()));
    filteredEntries.forEach(
        list -> {
          ContentList retrievedList = new ObjectMapper().convertValue(list, ContentList.class);
          Assert.assertTrue(conceptUUIDsList.contains(retrievedList.getConcept().getUuid()));
        });
    ;
  }

  @Test
  public void searchByConceptUUIDReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = new UUID[] {UUID.randomUUID()};

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, null, null, null, null);

    assertThat(filteredEntries.size(), is(0));
  }

  @Test
  public void searchByListTypeReturnsAllEntriesWithThisListType() {
    collection.insertMany(TEST_DATA);

    String tListType = TEST_DATA_LIST_TYPE.get("listType").toString();

    Concept concept =
        new Concept(
            (UUID) TEST_DATA_LIST_TYPE.get("conceptUUID"),
            (String) TEST_DATA_LIST_TYPE.get("conceptPrefLabel"));

    ContentList expectedList =
        new ContentList.Builder()
            .withUuid((UUID) TEST_DATA_LIST_TYPE.get("uuid"))
            .withTitle((String) TEST_DATA_LIST_TYPE.get("title"))
            .withConcept(concept)
            .withListType((String) TEST_DATA_LIST_TYPE.get("listType"))
            .build();

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, null, tListType, null, null, null);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredEntries.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchByListTypeReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = new UUID[] {};
    String tListType = "NonExistent";

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, tListType, null, null, null);

    assertThat(filteredEntries.size(), is(0));
  }

  @Test
  public void searchBySearchTermReturnsAllEntriesWithThisTermInTitle() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = null;
    String tListType = null;
    String tSearchTerm = "LookFor";

    Concept concept =
        new Concept(
            (UUID) TEST_DATA_TITLE.get("conceptUUID"),
            (String) TEST_DATA_TITLE.get("conceptPrefLabel"));

    ContentList expectedList =
        new ContentList.Builder()
            .withUuid((UUID) TEST_DATA_TITLE.get("uuid"))
            .withTitle((String) TEST_DATA_TITLE.get("title"))
            .withConcept(concept)
            .withListType((String) TEST_DATA_TITLE.get("listType"))
            .build();

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, tListType, tSearchTerm, null, null);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredEntries.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchBySearchTermReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = null;
    String tListType = null;
    String tSearchTerm = "NonExistent";

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, tListType, tSearchTerm, null, null);

    assertThat(filteredEntries.size(), is(0));
  }

  @Test
  public void searchByConceptUUIDListTypeAndSearchTermReturnsAllListsMatchingAllCriteria() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = new UUID[] {(UUID) TEST_DATA_All.get("conceptUUID")};
    String tListType = TEST_DATA_All.get("listType").toString();
    String tSearchTerm = "MatchAll";

    Concept concept =
        new Concept(
            (UUID) TEST_DATA_All.get("conceptUUID"),
            (String) TEST_DATA_All.get("conceptPrefLabel"));

    ContentList expectedList =
        new ContentList.Builder()
            .withUuid((UUID) TEST_DATA_All.get("uuid"))
            .withTitle((String) TEST_DATA_All.get("title"))
            .withConcept(concept)
            .withListType((String) TEST_DATA_All.get("listType"))
            .build();

    List<Document> filteredLists =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, tListType, tSearchTerm, null, null);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredLists.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchByWebUrlReturnsAllListsWithThisWebUrl() {
    collection.insertMany(TEST_DATA);

    String webUrl = TEST_DATA_All.get("webUrl").toString();

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(COLLECTION_NAME, null, null, null, webUrl, null);
    filteredEntries.forEach(
        entry -> {
          assertThat(entry.get("webUrl"), is(webUrl));
        });

    assertThat(filteredEntries.size(), is(2));
  }

  @Test
  public void searchByStandfirstReturnsAllListsWithThisStandfirst() {
    collection.insertMany(TEST_DATA);

    String standfirst = TEST_DATA_All.get("standfirst").toString();

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, null, null, null, null, standfirst);
    filteredEntries.forEach(
        entry -> {
          assertThat(entry.get("standfirst"), is(standfirst));
        });

    assertThat(filteredEntries.size(), is(1));
  }

  @Test
  public void searchByTitleWebUrlStandfirstReturnsAllListsWithThisTitleWebUrlAndStandfirst() {
    collection.insertMany(TEST_DATA);

    String webUrl = TEST_DATA_All.get("webUrl").toString();
    String standfirst = TEST_DATA_All.get("standfirst").toString();
    String searchTerm = TEST_DATA_All.get("title").toString();

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, null, null, searchTerm, webUrl, standfirst);
    filteredEntries.forEach(
        entry -> {
          assertThat(entry.get("title"), is(searchTerm));
          assertThat(entry.get("webUrl"), is(webUrl));
          assertThat(entry.get("standfirst"), is(standfirst));
        });

    assertThat(filteredEntries.size(), is(1));
  }

  @Test
  public void searchByConceptUUIDListTypeAndSearchTermReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = new UUID[] {UUID.randomUUID()};
    String tListType = "NonExistent";
    String tSearchTerm = "NonExistent";
    String webUrl = "NonExistent";
    String standfirst = "NonExistent";

    List<Document> filteredEntries =
        mongoDocumentStoreService.filterCollection(
            COLLECTION_NAME, tConceptUUIDs, tListType, tSearchTerm, webUrl, standfirst);

    assertThat(filteredEntries.size(), is(0));
  }
}
