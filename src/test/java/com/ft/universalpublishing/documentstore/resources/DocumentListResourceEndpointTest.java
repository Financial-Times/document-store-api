package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMapOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.handler.ContentListValidationHandler;
import com.ft.universalpublishing.documentstore.handler.ExtractConceptHandler;
import com.ft.universalpublishing.documentstore.handler.FindListByConceptAndTypeHandler;
import com.ft.universalpublishing.documentstore.handler.FindListByUuidHandler;
import com.ft.universalpublishing.documentstore.handler.GetConcordedConceptsHandler;
import com.ft.universalpublishing.documentstore.handler.Handler;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.handler.UuidValidationHandler;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.Concordance;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Identifier;
import com.ft.universalpublishing.documentstore.model.read.ListItem;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiService;
import com.ft.universalpublishing.documentstore.service.PublicConcordancesApiService;
import com.ft.universalpublishing.documentstore.target.ApplyConcordedConceptToListTarget;
import com.ft.universalpublishing.documentstore.target.DeleteDocumentTarget;
import com.ft.universalpublishing.documentstore.target.Target;
import com.ft.universalpublishing.documentstore.target.WriteDocumentTarget;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentListResourceEndpointTest {
  private static final MongoDocumentStoreService documentStoreService =
      mock(MongoDocumentStoreService.class);
  private static final PublicConceptsApiService publicConceptsApiService =
      mock(PublicConceptsApiService.class);
  private static final PublicConcordancesApiService publicConcordancesApiService =
      mock(PublicConcordancesApiService.class);
  private static final ContentListValidator contentListValidator = mock(ContentListValidator.class);
  private static final UuidValidator uuidValidator = mock(UuidValidator.class);
  private static final String API_URL_PREFIX_CONTENT = "localhost";
  private static final String RESOURCE_TYPE = "lists";
  private static final UUID CONCEPT_UUID = UUID.randomUUID();
  private static final UUID[] CONCEPT_UUIDS = new UUID[] {CONCEPT_UUID};
  private static final String CONCEPT_PREF_LABEL = "World";
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final Concept CONCEPT = new Concept(CONCEPT_UUID, CONCEPT_PREF_LABEL);

  private static final ResourceExtension resources =
      ResourceExtension.builder().addResource(new DocumentResource(getCollectionMap())).build();

  private String uuid;
  private Document listAsDocument;
  private Document listWithoutConceptAsDocument;
  private ContentList outboundList;
  private ContentList outboundListWithoutConcept;
  private String uuidPath;

  @SuppressWarnings("unchecked")
  public DocumentListResourceEndpointTest() {
    this.uuid = UUID.randomUUID().toString();
    ContentList contentList =
        getContentList(uuid, UUID.randomUUID().toString(), UUID.randomUUID().toString(), true);
    this.listAsDocument = new Document(objectMapper.convertValue(contentList, Map.class));
    this.outboundList = getOutboundContentList(contentList, true);

    ContentList contentListWithoutConcept =
        getContentListWithoutConcept(
            uuid, UUID.randomUUID().toString(), UUID.randomUUID().toString());
    this.listWithoutConceptAsDocument =
        new Document(objectMapper.convertValue(contentListWithoutConcept, Map.class));
    this.outboundListWithoutConcept = getOutboundContentList(contentListWithoutConcept, false);
    this.uuidPath = "/" + RESOURCE_TYPE + "/" + uuid;
  }

  private static Map<Pair<String, Operation>, HandlerChain> getCollectionMap() {
    Handler uuidValidationHandler = new UuidValidationHandler(uuidValidator);
    Handler extractConceptHandler = new ExtractConceptHandler();
    Handler contentListValidationHandler = new ContentListValidationHandler(contentListValidator);
    Handler findListByUuidHandler = new FindListByUuidHandler(documentStoreService);
    Handler findListByConceptAndTypeHandler =
        new FindListByConceptAndTypeHandler(documentStoreService);
    Handler getConcordedConceptsHandler =
        new GetConcordedConceptsHandler(publicConcordancesApiService);
    Target writeDocument = new WriteDocumentTarget(documentStoreService);
    Target deleteDocument = new DeleteDocumentTarget(documentStoreService);
    Target applyConcordedConceptToList =
        new ApplyConcordedConceptToListTarget(publicConceptsApiService, API_URL_PREFIX_CONTENT);
    final Map<Pair<String, Operation>, HandlerChain> collections = new HashMap<>();
    collections.put(
        new Pair<>("lists", Operation.GET_BY_ID),
        new HandlerChain()
            .addHandlers(uuidValidationHandler, findListByUuidHandler)
            .setTarget(applyConcordedConceptToList));
    collections.put(
        new Pair<>("lists", Operation.GET_FILTERED),
        new HandlerChain()
            .addHandlers(
                extractConceptHandler, getConcordedConceptsHandler, findListByConceptAndTypeHandler)
            .setTarget(applyConcordedConceptToList));
    collections.put(
        new Pair<>("lists", Operation.ADD),
        new HandlerChain()
            .addHandlers(uuidValidationHandler, contentListValidationHandler)
            .setTarget(writeDocument));
    collections.put(
        new Pair<>("lists", Operation.REMOVE),
        new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));

    return collections;
  }

  private static ContentList getContentList(
      String listUuid, String firstContentUuid, String secondContentUuid, boolean addConcept) {
    ListItem contentItem1 = new ListItem();
    contentItem1.setUuid(firstContentUuid);
    ListItem contentItem2 = new ListItem();
    contentItem2.setUuid(secondContentUuid);
    List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);

    ContentList.Builder builder =
        new ContentList.Builder().withUuid(UUID.fromString(listUuid)).withItems(content);

    if (addConcept) {
      Concept concept = new Concept(CONCEPT_UUID, CONCEPT_PREF_LABEL);
      URI id = URI.create(String.format("http://api.ft.com/things/%s", concept.getUuid()));
      concept.setId(id);
      builder.withConcept(concept);
    }

    return builder.build();
  }

  private static ContentList getContentListWithoutConcept(
      String listUuid, String firstContentUuid, String secondContentUuid) {
    ListItem contentItem1 = new ListItem();
    contentItem1.setUuid(firstContentUuid);
    ListItem contentItem2 = new ListItem();
    contentItem2.setUuid(secondContentUuid);
    List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);

    return new ContentList.Builder().withUuid(UUID.fromString(listUuid)).withItems(content).build();
  }

  private static ContentList getOutboundContentList(ContentList contentList, boolean addConcept) {
    ContentList outboundList =
        getContentList(
            contentList.getUuid(),
            contentList.getItems().get(0).getUuid(),
            contentList.getItems().get(1).getUuid(),
            addConcept);

    outboundList.addIds();
    outboundList.addApiUrls(API_URL_PREFIX_CONTENT);
    outboundList.removePrivateFields();
    return outboundList;
  }

  @BeforeEach
  public void setup() {
    reset(documentStoreService);
    reset(publicConceptsApiService);
    reset(publicConcordancesApiService);
    reset(contentListValidator);
    reset(uuidValidator);
    when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class)))
        .thenReturn(DocumentWritten.created(listAsDocument));
  }

  // WRITE

  @Test
  public void shouldReturn201ForNewDocument() {
    Response clientResponse = writeDocument(uuidPath, listAsDocument);
    assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
    verify(documentStoreService).write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class));
  }

  @Test
  public void shouldReturn200ForUpdatedContent() {
    when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class)))
        .thenReturn(DocumentWritten.updated(listAsDocument));

    Response clientResponse = writeDocument(uuidPath, listAsDocument);
    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void shouldReturn400OnWriteWhenUuidNotValid() {
    doThrow(new ValidationException("Invalid Uuid"))
        .when(uuidValidator)
        .validate(anyString(), anyString());
    Response clientResponse = writeDocument(uuidPath, listAsDocument);

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Invalid Uuid", clientResponse);
  }

  @Test
  public void shouldReturn503WhenCannotAccessExternalSystem() {
    when(documentStoreService.write(eq(RESOURCE_TYPE), any()))
        .thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));

    Response clientResponse = writeDocument(uuidPath, listAsDocument);

    assertThat("", clientResponse, hasProperty("status", equalTo(503)));
  }

  @Test
  public void shouldReturn500WhenExternalSystemHasAnInternalException() {
    when(documentStoreService.write(eq(RESOURCE_TYPE), any()))
        .thenThrow(
            new ExternalSystemInternalServerException(
                new IllegalArgumentException("Some bogus exception")));

    Response clientResponse = writeDocument(uuidPath, listAsDocument);

    assertThat("", clientResponse, hasProperty("status", equalTo(500)));
  }

  // REMOVE

  @Test
  public void shouldReturn200WhenDeletedSuccessfully() {
    Response clientResponse = resources.client().target(uuidPath).request().delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void shouldReturn200WhenDeletingNonExistentContentList() {
    doThrow(new DocumentNotFoundException(UUID.fromString(uuid)))
        .when(documentStoreService)
        .delete(eq(RESOURCE_TYPE), any(UUID.class));

    Response clientResponse = resources.client().target(uuidPath).request().delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void shouldReturn400OnDeleteWhenUuidNotValid() {
    doThrow(new ValidationException("Invalid Uuid"))
        .when(uuidValidator)
        .validate(anyString(), anyString());
    Response clientResponse = resources.client().target(uuidPath).request().delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Invalid Uuid", clientResponse);
  }

  @Test
  public void shouldReturn503OnDeleteWhenMongoIsntReachable() {
    doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"))
        .when(documentStoreService)
        .delete(eq(RESOURCE_TYPE), any(UUID.class));

    Response clientResponse = resources.client().target(uuidPath).request().delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
  }

  // READ
  @Test
  public void shouldReturn200WhenReadSuccessfully()
      throws JsonMappingException, JsonProcessingException {
    Map conceptDocument = (Map) listAsDocument.get("concept");
    Concept concept =
        new Concept(
            UUID.fromString(conceptDocument.get("uuid").toString()),
            conceptDocument.get("prefLabel").toString());
    URI id = URI.create(String.format("http://api.ft.com/things/%s", concept.getUuid()));
    concept.setId(id);
    Concept resultConcept =
        objectMapper.readValue(objectMapper.writeValueAsString(concept), Concept.class);

    when(publicConceptsApiService.getUpToDateConcept(eq(concept.getUuid().toString())))
        .thenReturn(resultConcept);
    when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class)))
        .thenReturn(listAsDocument);
    Response clientResponse = resources.client().target(uuidPath).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    final ContentList retrievedDocument = clientResponse.readEntity(ContentList.class);
    verify(publicConceptsApiService).getUpToDateConcept(eq(concept.getUuid().toString()));
    verify(documentStoreService).findByUuid(eq(RESOURCE_TYPE), any(UUID.class));
    assertThat("inboundListAsDocument", retrievedDocument, equalTo(outboundList));
  }

  @Test
  public void shouldReturnListWithoutConceptWhenReadSuccessfully()
      throws JsonMappingException, JsonProcessingException {
    when(publicConceptsApiService.getUpToDateConcept(eq(null))).thenReturn(null);
    when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class)))
        .thenReturn(listWithoutConceptAsDocument);
    Response clientResponse = resources.client().target(uuidPath).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    final ContentList retrievedDocument = clientResponse.readEntity(ContentList.class);
    verify(publicConceptsApiService).getUpToDateConcept(eq(null));
    verify(documentStoreService).findByUuid(eq(RESOURCE_TYPE), any(UUID.class));
    assertThat("inboundListAsDocument", retrievedDocument, equalTo(outboundListWithoutConcept));
  }

  @Test
  public void shouldReturn404WhenContentNotFound() {
    when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class)))
        .thenThrow(new DocumentNotFoundException(UUID.fromString(uuid)));

    Response clientResponse = resources.client().target(uuidPath).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    validateErrorMessage("Requested item does not exist", clientResponse);
  }

  @Test
  public void shouldReturn400OnReadWhenUuidNotValid() {
    doThrow(new ValidationException("Invalid Uuid"))
        .when(uuidValidator)
        .validate(anyString(), anyString());
    Response clientResponse = resources.client().target(uuidPath).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Invalid Uuid", clientResponse);
  }

  @Test
  public void shouldReturn503OnReadWhenMongoIsntReachable() {
    doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"))
        .when(documentStoreService)
        .findByUuid(eq(RESOURCE_TYPE), any(UUID.class));

    Response clientResponse = resources.client().target(uuidPath).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
  }

  // FIND LIST BY CONCEPT AND TYPE
  @Test
  public void shouldReturn200ForDocumentFoundByConceptAndType()
      throws JsonMappingException, JsonProcessingException {
    String type = "TopStories";
    String typeParam = "curatedTopStoriesFor";

    List<Concordance> concordances = new ArrayList<>();
    Concept concept = new Concept(CONCEPT_UUID, CONCEPT.getPrefLabel());
    URI id = URI.create(String.format("http://api.ft.com/things/%s", concept.getUuid()));
    concept.setId(id);
    concordances.add(
        new Concordance(
            concept, new Identifier("http://api.ft.com/system/UPP", CONCEPT_UUID.toString())));
    Concept resultConcept =
        objectMapper.readValue(objectMapper.writeValueAsString(concept), Concept.class);
    when(publicConcordancesApiService.getUPPConcordances(eq(CONCEPT_UUID.toString())))
        .thenReturn(concordances);
    when(documentStoreService.findByConceptAndType(eq(RESOURCE_TYPE), eq(CONCEPT_UUIDS), eq(type)))
        .thenReturn(listAsDocument);
    when(publicConceptsApiService.getUpToDateConcept(eq(concept.getUuid().toString())))
        .thenReturn(resultConcept);

    Response clientResponse =
        resources
            .client()
            .target("/lists")
            .queryParam(typeParam, CONCEPT_UUID.toString())
            .request()
            .get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    final ContentList retrievedDocument = clientResponse.readEntity(ContentList.class);
    verify(publicConceptsApiService).getUpToDateConcept(eq(concept.getUuid().toString()));
    verify(publicConcordancesApiService).getUPPConcordances(eq(CONCEPT_UUID.toString()));
    verify(documentStoreService)
        .findByConceptAndType(eq(RESOURCE_TYPE), eq(CONCEPT_UUIDS), eq(type));
    assertThat("documents were not the same", retrievedDocument, equalTo(outboundList));
  }

  @Test
  public void shouldReturn404ForDocumentNotFoundByConceptAndType() {
    String type = "TopStories";
    String typeParam = "curatedTopStoriesFor";

    when(documentStoreService.findByConceptAndType(eq(RESOURCE_TYPE), eq(CONCEPT_UUIDS), eq(type)))
        .thenReturn(null);
    Response clientResponse =
        resources
            .client()
            .target("/lists")
            .queryParam(typeParam, CONCEPT_UUID.toString())
            .request()
            .get();

    verify(documentStoreService)
        .findByConceptAndType(eq(RESOURCE_TYPE), eq(CONCEPT_UUIDS), eq(type));
    assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
  }

  @Test
  public void shouldReturn400ForNoQueryParameterSupplied() {
    Response clientResponse = resources.client().target("/lists").request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Expected at least one query parameter", clientResponse);
  }

  @Test
  public void shouldReturn400ForNoValidQueryParameterSupplied() {
    String invalidTypeParam = "invalidType";

    Response clientResponse =
        resources
            .client()
            .target("/lists")
            .queryParam(invalidTypeParam, CONCEPT_UUID.toString())
            .request()
            .get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage(
        "Expected at least one query parameter of the form \"curated<listType>For\"",
        clientResponse);
  }

  @Test
  public void shouldReturn400ForNoValidUUID() {
    String conceptID = "123";
    String typeParam = "curatedTopStoriesFor";

    Response clientResponse =
        resources.client().target("/lists").queryParam(typeParam, conceptID).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("The concept ID is not a valid UUID", clientResponse);
  }

  // OTHER
  @Test
  public void shouldReturn405ForPost() {
    Response clientResponse = resources.client().target(uuidPath).request().post(Entity.json(null));

    assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
  }

  private Response writeDocument(String writePath, Document document) {
    return resources
        .client()
        .target(writePath)
        .request()
        .put(Entity.entity(document, MediaType.APPLICATION_JSON));
  }

  private void validateErrorMessage(
      String expectedErrorMessage, javax.ws.rs.core.Response clientResponse) {
    final ErrorEntity responseBodyMessage = clientResponse.readEntity(ErrorEntity.class);
    assertThat(
        "message", responseBodyMessage, hasProperty("message", equalTo(expectedErrorMessage)));
  }
}
