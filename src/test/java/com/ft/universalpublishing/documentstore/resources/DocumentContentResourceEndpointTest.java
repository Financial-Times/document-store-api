package com.ft.universalpublishing.documentstore.resources;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.handler.ExtractUuidsHandler;
import com.ft.universalpublishing.documentstore.handler.Handler;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.handler.MultipleUuidValidationHandler;
import com.ft.universalpublishing.documentstore.handler.PreSaveFieldRemovalHandler;
import com.ft.universalpublishing.documentstore.handler.UuidValidationHandler;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.target.DeleteDocumentTarget;
import com.ft.universalpublishing.documentstore.target.FindMultipleResourcesByUuidsTarget;
import com.ft.universalpublishing.documentstore.target.FindResourceByUuidTarget;
import com.ft.universalpublishing.documentstore.target.Target;
import com.ft.universalpublishing.documentstore.target.WriteDocumentTarget;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.bson.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DocumentContentResourceEndpointTest {

  private String uuid;
  private Document document;
  private String contentPath;
  private final static MongoDocumentStoreService documentStoreService = mock(MongoDocumentStoreService.class);

  private final static ContentListValidator contentListValidator = mock(ContentListValidator.class);
  private final static UuidValidator uuidValidator = mock(UuidValidator.class);
  private static final String RESOURCE_TYPE = "content";

  public DocumentContentResourceEndpointTest() {
    this.uuid = UUID.randomUUID().toString();
    this.document = getContent(uuid);
    this.contentPath = "/" + RESOURCE_TYPE + "/" + uuid;
  }

  private static Document getContent(String uuid) {
    Date lastPublicationDate = new Date();
    Map<String, Object> content = new HashMap<>();
    content.put("uuid", uuid);
    content.put("title", "Here's the news");
    content.put("bodyXML", "xmlBody");
    content.put("storyPackage", UUID.randomUUID().toString());
    content.put("contentPackage", UUID.randomUUID().toString());
    content.put("publishedDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(lastPublicationDate));
    return new Document(content);
  }

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new DocumentResource(getCollectionMap()))
      .build();

  private static final Map<String, String> templates = new HashMap<>();
  static {
    templates.put("http://www.ft.com/ontology/content/Article", "/content/{{id}}");
    templates.put("http://www.ft.com/ontology/content/ImageSet", "/content/{{id}}");
  }

  private static Map<Pair<String, Operation>, HandlerChain> getCollectionMap() {
    Handler uuidValidationHandler = new UuidValidationHandler(uuidValidator);
    Handler multipleUuidValidationHandler = new MultipleUuidValidationHandler(uuidValidator);
    Handler extractUuidsHandlers = new ExtractUuidsHandler();
    Handler preSaveFieldRemovalHandler = new PreSaveFieldRemovalHandler();
    Target findResourceByUuid = new FindResourceByUuidTarget(documentStoreService);
    Target findMultipleResourcesByUuidsTarget = new FindMultipleResourcesByUuidsTarget(documentStoreService);
    Target writeDocument = new WriteDocumentTarget(documentStoreService);
    Target deleteDocument = new DeleteDocumentTarget(documentStoreService);

    final Map<Pair<String, Operation>, HandlerChain> collections = new HashMap<>();
    collections.put(new Pair<>("content", Operation.GET_FILTERED),
            new HandlerChain().addHandlers(extractUuidsHandlers, multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
    collections.put(new Pair<>("content", Operation.GET_MULTIPLE_FILTERED),
            new HandlerChain().addHandlers(multipleUuidValidationHandler).setTarget(findMultipleResourcesByUuidsTarget));
    collections.put(new Pair<>("content", Operation.GET_BY_ID),
            new HandlerChain().addHandlers(uuidValidationHandler).setTarget(findResourceByUuid));
    collections.put(new Pair<>("content", Operation.ADD),
            new HandlerChain().addHandlers(uuidValidationHandler, preSaveFieldRemovalHandler).setTarget(writeDocument));
    collections.put(new Pair<>("content", Operation.REMOVE),
            new HandlerChain().addHandlers(uuidValidationHandler).setTarget(deleteDocument));


    return collections;
  }

  @Before
  public void setup() {
    reset(documentStoreService);
    reset(contentListValidator);
    reset(uuidValidator);
    when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class))).thenReturn(DocumentWritten.created(document));
  }

  //WRITE

  @Test
  public void shouldReturn201ForNewDocument() {
    Response clientResponse = writeDocument(contentPath, document);
    assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
    verify(documentStoreService).write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class));
  }

  @Test
  public void shouldReturn200ForUpdatedContent() {
    when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class))).thenReturn(DocumentWritten.updated(document));

    Response clientResponse = writeDocument(contentPath, document);
    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void shouldReturn400OnWriteWhenUuidNotValid() {
    doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
    Response clientResponse = writeDocument(contentPath, document);

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Invalid Uuid", clientResponse);
  }

  @Test
  public void shouldReturn503WhenCannotAccessExternalSystem() {
    when(documentStoreService.write(eq(RESOURCE_TYPE), any())).thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));

    Response clientResponse = writeDocument(contentPath, document);

    assertThat("", clientResponse, hasProperty("status", equalTo(503)));
  }

  @Test
  public void storyPackageIsRemoved() {
		UUID uuid = UUID.randomUUID();
		Map<String, Object> content = getContent(uuid.toString());
	    Document docInitial = new Document(content);
		content.remove("storyPackage");
		Document docRemove = new Document(content);
		when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class))).thenReturn(
				DocumentWritten.updated(docRemove));

		Response clientResponse = writeDocument(contentPath, docInitial);
	    Document resultDoc = clientResponse.readEntity(Document.class);
	    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
	    assertEquals(resultDoc, docRemove);
  }

  @Test
  public void shouldHaveFieldsRemovedWhenWritten() throws Exception {
    final Document localDoc = getContent(UUID.randomUUID().toString());

    final ArgumentCaptor<Map> contentCaptor = ArgumentCaptor.forClass(Map.class);
    when(documentStoreService.write(eq(RESOURCE_TYPE), contentCaptor.capture())).thenReturn(DocumentWritten.created(localDoc));

    final Response response = writeDocument(contentPath, localDoc);
    assertThat(response.getStatus(), is(201));

    final Map contentMap = contentCaptor.getValue();
    assertThat(contentMap.containsKey("storyPackage"), is(false));
    assertThat(contentMap.containsKey("contentPackage"), is(false));
  }

  //REMOVE

  @Test
  public void shouldReturn200WhenDeletedSuccessfully() {
    Response clientResponse = resources.client().target(contentPath).request()
            .delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void shouldReturn200WhenDeletingNonExistentContent() {
    doThrow(new DocumentNotFoundException(UUID.fromString(uuid))).when(documentStoreService).delete(eq(RESOURCE_TYPE), any(UUID.class));

    Response clientResponse = resources.client().target(contentPath).request()
            .delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void shouldReturn400OnDeleteWhenUuidNotValid() {
    doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
    Response clientResponse = resources.client().target(contentPath).request()
            .delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Invalid Uuid", clientResponse);
  }

  @Test
  public void shouldReturn503OnDeleteWhenMongoIsntReachable() {
    doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).delete(eq(RESOURCE_TYPE), any(UUID.class));

    Response clientResponse = resources.client().target(contentPath).request()
            .delete();

    assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
  }

  //READ
  @Test
  public void shouldReturn200WhenReadSuccessfully() {
    when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class))).thenReturn(document);
    Response clientResponse = resources.client().target(contentPath)
            .request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    final Document retrievedDocument = clientResponse.readEntity(Document.class);
    assertThat("document", retrievedDocument, equalTo(document));
  }

  @Test
  public void thatMultipleUUIDsCanBeRequested() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    Set<UUID> uuids = new LinkedHashSet<>();
    uuids.add(uuid1);
    uuids.add(uuid2);

    String id1 = uuid1.toString();
    Document document1 = getContent(id1);

    String id2 = uuid2.toString();
    Document document2 = getContent(id2);

    Set<Map<String,Object>> documents = new LinkedHashSet<>();
    documents.add(document1);
    documents.add(document2);

    when(documentStoreService.findByUuids(eq(RESOURCE_TYPE), eq(uuids))).thenReturn(documents);

    Response clientResponse = resources.client().target("/content")
            .queryParam("uuid", id1)
            .queryParam("uuid", id2)
            .request()
            .get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

    @SuppressWarnings("unchecked")
    final List<Map<String,Object>> actual = clientResponse.readEntity(List.class);
    assertThat("documents", actual.size(), equalTo(2));

    List<String> actualIds = actual.stream().map(m -> (String)m.get("uuid")).collect(Collectors.toList());

    assertThat("document list", actualIds, contains(id1, id2));
  }
  
  @Test
  public void thatMultipleUUIDRequestWithEmptyParamatersReturns400() {
    Response clientResponse = resources.client().target("/content")
            .request()
            .get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
  }

  @Test
  public void thatSubsetOfFoundUUIDsIsReturned() {
    UUID uuid1 = UUID.randomUUID();
    String id1 = uuid1.toString();

    UUID uuid2 = UUID.randomUUID();
    String id2 = uuid2.toString();
    Document document2 = getContent(id2);

    Set<UUID> uuids = new LinkedHashSet<>();
    uuids.add(uuid1);
    uuids.add(uuid2);

    when(documentStoreService.findByUuids(eq(RESOURCE_TYPE), eq(uuids))).thenReturn(Collections.singleton(document2));

    Response clientResponse = resources.client().target("/content")
            .queryParam("uuid", id1)
            .queryParam("uuid", id2)
            .request()
            .get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

    @SuppressWarnings("unchecked")
    final List<Map<String,Object>> actual = clientResponse.readEntity(List.class);
    assertThat("documents", actual.size(), equalTo(1));

    List<String> actualIds = actual.stream().map(m -> (String)m.get("uuid")).collect(Collectors.toList());

    assertThat("document list", actualIds, equalTo(Collections.singletonList(id2)));
  }

  @Test
  public void thatReturns200EvenIfNoUUIDsAreFound() {
    UUID uuid1 = UUID.randomUUID();
    String id1 = uuid1.toString();
    when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class))).thenThrow(new DocumentNotFoundException(null));

    UUID uuid2 = UUID.randomUUID();
    String id2 = uuid2.toString();

    Response clientResponse = resources.client().target("/content")
            .queryParam("uuid", id1)
            .queryParam("uuid", id2)
            .request()
            .get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

    @SuppressWarnings("rawtypes")
    final List actual = clientResponse.readEntity(List.class);
    assertThat("documents", actual.size(), equalTo(0));
  }

  @Test
  public void thatMultipleUUIDsCanBeRequestedWithHttpPost() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    List<UUID> uuidList = Arrays.asList(uuid1, uuid2);
    Set<UUID> uuidSet = new LinkedHashSet<>();
    uuidSet.add(uuid1);
    uuidSet.add(uuid2);
    String uuidString1 = uuid1.toString();
    Document document1 = getContent(uuidString1);
    String uuidString2 = uuid2.toString();
    Document document2 = getContent(uuidString2);
    Set<Map<String,Object>> documents = new LinkedHashSet<>();
    documents.add(document1);
    documents.add(document2);

    when(documentStoreService.findByUuids(eq(RESOURCE_TYPE), eq(uuidSet))).thenReturn(documents);

    Response clientResponse = resources.client().target("/content?mget=true")
            .request()
            .post(Entity.entity(uuidList, MediaType.APPLICATION_JSON));

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

    @SuppressWarnings("unchecked")
    final List<Map<String,Object>> actual = clientResponse.readEntity(List.class);
    assertThat("documents", actual.size(), equalTo(2));

    List<String> actualIds = actual.stream().map(m -> (String)m.get("uuid")).collect(Collectors.toList());
    assertThat("document list", actualIds, contains(uuidString1, uuidString2));
  }

  @Test
  public void thatMultipleUUIDRequestWithEmptyListIsOk() {
    Response clientResponse = resources.client().target("/content?mget=true")
            .request()
            .post(Entity.entity(Collections.emptyList(), MediaType.APPLICATION_JSON));

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
  }

  @Test
  public void thatSubsetOfFoundUUIDsIsReturnedWithHttpPost() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    List<UUID> uuidList = Arrays.asList(uuid1, uuid2);
    String id2 = uuid2.toString();
    Document document = getContent(id2);
    Set<UUID> uuidSet = new LinkedHashSet<>();
    uuidSet.add(uuid1);
    uuidSet.add(uuid2);

    when(documentStoreService.findByUuids(eq(RESOURCE_TYPE), eq(uuidSet))).thenReturn(Collections.singleton(document));

    Response clientResponse = resources.client().target("/content?mget=true")
            .request()
            .post(Entity.entity(uuidList, MediaType.APPLICATION_JSON));

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));

    @SuppressWarnings("unchecked")
    final List<Map<String,Object>> actual = clientResponse.readEntity(List.class);
    assertThat("documents", actual.size(), equalTo(1));

    List<String> actualIds = actual.stream().map(m -> (String)m.get("uuid")).collect(Collectors.toList());
    assertThat("document list", actualIds, equalTo(Collections.singletonList(id2)));
  }

  @Test
  public void thatMultipleUUIDRequestWithoutQueryParamIsNotOk() {
    Response clientResponse = resources.client().target("/content")
            .request()
            .post(Entity.entity(Collections.emptyList(), MediaType.APPLICATION_JSON));

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
  }

  @Test
  public void shouldReturn404WhenContentNotFound() {
    when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class)))
            .thenThrow(new DocumentNotFoundException(UUID.fromString(uuid)));

    Response clientResponse = resources.client().target(contentPath).request()
            .get(Response.class);

    assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    validateErrorMessage("Requested item does not exist", clientResponse);
  }

  @Test
  public void shouldReturn400OnReadWhenUuidNotValid() {
    doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
    Response clientResponse = resources.client().target(contentPath).request()
            .get(Response.class);

    assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    validateErrorMessage("Invalid Uuid", clientResponse);
  }

  @Test
  public void shouldReturn503OnReadWhenMongoIsntReachable() {
    doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).findByUuid(eq(RESOURCE_TYPE), any(UUID.class));

    Response clientResponse = resources.client().target(contentPath).request()
            .get(Response.class);

    assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
  }

  //OTHER
  @Test
  public void shouldReturn405ForPost() {
    Response clientResponse = resources.client().target(contentPath)
            .request()
            .post(Entity.json(null));

    assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
  }

  private Response writeDocument(String writePath, Document document) {
    return resources.client()
            .target(writePath).request()
            .put(Entity.entity(document, MediaType.APPLICATION_JSON));
  }

  private void validateErrorMessage(String expectedErrorMessage, Response clientResponse) {
    final ErrorEntity responseBodyMessage = clientResponse.readEntity(ErrorEntity.class);
    assertThat("message", responseBodyMessage, hasProperty("message", equalTo(expectedErrorMessage)));
  }

}
