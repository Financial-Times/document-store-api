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
  private static final String DB_COLLECTION = "generic-lists";

  private static final Map<String, Object> TEST_DATA_All =
      ImmutableMap.<String, Object>builder()
          .put("uuid", UUID.randomUUID())
          .put("conceptUUID", UUID.randomUUID())
          .put("conceptPrefLabel", "Concept MatchAll")
          .put("title", "Title MatchAll")
          .put("listType", "MatchAll")
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

  private static final Document LIST0 =
      new Document()
          .append("uuid", TEST_DATA_All.get("uuid"))
          .append("title", TEST_DATA_All.get("title"))
          .append("concept", TEST_CONCEPT0)
          .append("listType", TEST_DATA_All.get("listType"));

  private static final Document TEST_CONCEPT1 =
      new Document()
          .append("uuid", TEST_DATA_CONCEPT.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_CONCEPT.get("conceptPrefLabel"));

  private static final Document LIST1 =
      new Document()
          .append("uuid", TEST_DATA_CONCEPT.get("uuid"))
          .append("title", TEST_DATA_CONCEPT.get("title"))
          .append("concept", TEST_CONCEPT1)
          .append("listType", TEST_DATA_CONCEPT.get("listType"));

  private static final Document TEST_CONCEPT2 =
      new Document()
          .append("uuid", TEST_DATA_TITLE.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_TITLE.get("conceptPrefLabel"));

  private static final Document LIST2 =
      new Document()
          .append("uuid", TEST_DATA_TITLE.get("uuid"))
          .append("title", TEST_DATA_TITLE.get("title"))
          .append("concept", TEST_CONCEPT2)
          .append("listType", TEST_DATA_TITLE.get("listType"));

  private static final Document TEST_CONCEPT3 =
      new Document()
          .append("uuid", TEST_DATA_LIST_TYPE.get("conceptUUID").toString())
          .append("prefLabel", TEST_DATA_LIST_TYPE.get("conceptPrefLabel"));

  private static final Document LIST3 =
      new Document()
          .append("uuid", TEST_DATA_LIST_TYPE.get("uuid"))
          .append("title", TEST_DATA_LIST_TYPE.get("title"))
          .append("concept", TEST_CONCEPT3)
          .append("listType", TEST_DATA_LIST_TYPE.get("listType"));

  private static final List<Document> TEST_DATA = Arrays.asList(LIST0, LIST1, LIST2, LIST3);

  @RegisterExtension
  static EmbeddedMongoExtension mongo =
      EmbeddedMongoExtension.builder().dbName(DB_NAME).dbCollection(DB_COLLECTION).build();

  private MongoDocumentStoreService mongoDocumentStoreService;
  private MongoCollection<Document> collection;

  @BeforeEach
  public void setup() {
    MongoDatabase db = mongo.getDb();
    mongoDocumentStoreService =
        new MongoDocumentStoreService(db, Executors.newSingleThreadExecutor());
    mongoDocumentStoreService.applyIndexes();
    collection = db.getCollection(DB_COLLECTION);
  }

  @Test
  public void searchWithNoParamsAndNoListsShouldReturnEmptyArray() {
    UUID[] tConceptUUID = new UUID[] {};
    String tListType = null;
    String tSearchTerm = null;

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUID, tListType, tSearchTerm);

    assertThat(filteredLists.size(), is(0));
  }

  @Test
  public void searchWithNoParamsShouldReturnAllLists() {
    collection.insertMany(TEST_DATA);
    UUID[] tConceptUUIDs = null;
    String tListType = null;
    String tSearchTerm = null;

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);

    assertThat(filteredLists.size(), is(TEST_DATA.size()));
  }

  @Test
  public void searchByConceptUUIDReturnsAllListsWithThisConcept() {
    collection.insertMany(TEST_DATA);
    UUID[] tConceptUUIDs =
        new UUID[] {UUID.fromString(TEST_DATA_CONCEPT.get("conceptUUID").toString())};
    String tListType = null;
    String tSearchTerm = null;

    Concept concept =
        new Concept(
            (UUID) TEST_DATA_CONCEPT.get("conceptUUID"),
            (String) TEST_DATA_CONCEPT.get("conceptPrefLabel"));

    ContentList expectedList =
        new ContentList.Builder()
            .withUuid((UUID) TEST_DATA_CONCEPT.get("uuid"))
            .withTitle((String) TEST_DATA_CONCEPT.get("title"))
            .withConcept(concept)
            .withListType((String) TEST_DATA_CONCEPT.get("listType"))
            .build();

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredLists.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchByConceptUUIDReturnsAllListsForMultipleConcepts() {
    collection.insertMany(TEST_DATA);
    UUID[] tConceptUUIDs =
        new UUID[] {
          UUID.fromString(TEST_DATA_CONCEPT.get("conceptUUID").toString()),
          UUID.fromString(TEST_DATA_TITLE.get("conceptUUID").toString())
        };
    String tListType = null;
    String tSearchTerm = null;

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);
    List<UUID> conceptUUIDsList = Arrays.asList(tConceptUUIDs);

    assertThat(filteredLists.size(), is(conceptUUIDsList.size()));
    filteredLists.forEach(
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
    String tListType = null;
    String tSearchTerm = null;

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);

    assertThat(filteredLists.size(), is(0));
  }

  @Test
  public void searchByListTypeReturnsAllListsWithThisListType() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = null;
    String tListType = TEST_DATA_LIST_TYPE.get("listType").toString();
    String tSearchTerm = null;

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

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredLists.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchByListTypeReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = new UUID[] {};
    String tListType = "NonExistent";
    String tSearchTerm = null;

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);

    assertThat(filteredLists.size(), is(0));
  }

  @Test
  public void searchBySearchTermReturnsAllListsWithThisTermInTitle() {
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

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredLists.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchBySearchTermReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = null;
    String tListType = null;
    String tSearchTerm = "NonExistent";

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);

    assertThat(filteredLists.size(), is(0));
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
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);
    ContentList retrievedList =
        new ObjectMapper().convertValue(filteredLists.get(0), ContentList.class);

    assertThat(retrievedList, is(expectedList));
  }

  @Test
  public void searchByConceptUUIDListTypeAndSearchTermReturnsEmptyArrayWhenNoMatches() {
    collection.insertMany(TEST_DATA);

    UUID[] tConceptUUIDs = new UUID[] {UUID.randomUUID()};
    String tListType = "NonExistent";
    String tSearchTerm = "NonExistent";

    List<Document> filteredLists =
        mongoDocumentStoreService.filterLists(DB_COLLECTION, tConceptUUIDs, tListType, tSearchTerm);

    assertThat(filteredLists.size(), is(0));
  }
}
