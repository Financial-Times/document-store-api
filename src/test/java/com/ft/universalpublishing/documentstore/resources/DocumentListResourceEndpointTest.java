package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
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
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.BrandsMapper;
import com.ft.universalpublishing.documentstore.model.ContentMapper;
import com.ft.universalpublishing.documentstore.model.IdentifierMapper;
import com.ft.universalpublishing.documentstore.model.StandoutMapper;
import com.ft.universalpublishing.documentstore.model.TypeResolver;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.ListItem;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.transform.ContentBodyProcessingService;
import com.ft.universalpublishing.documentstore.transform.ModelBodyXmlTransformer;
import com.ft.universalpublishing.documentstore.transform.UriBuilder;
import com.ft.universalpublishing.documentstore.util.ContextBackedApiUriGeneratorProvider;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientResponse;

public class DocumentListResourceEndpointTest {

    private String uuid;
    private Document listAsDocument;
    private ContentList outboundList;
    private String uuidPath;

    private final static DocumentStoreService documentStoreService = mock(DocumentStoreService.class);
    private final static ContentListValidator contentListValidator = mock(ContentListValidator.class);
    private final static UuidValidator uuidValidator = mock(UuidValidator.class);
    private static final String API_URL_PREFIX_CONTENT = "localhost";
    private static final String RESOURCE_TYPE = "lists";

    @SuppressWarnings("unchecked")
    public DocumentListResourceEndpointTest() {
        this.uuid = UUID.randomUUID().toString();
        ContentList contentList = getContentList(uuid, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        this.listAsDocument = new Document(new ObjectMapper().convertValue(contentList, Map.class));
        this.outboundList = getOutboundContentList(contentList);
        this.uuidPath = "/" + RESOURCE_TYPE + "/" + uuid;
    }

    private static ContentList getContentList(String listUuid, String firstContentUuid, String secondContentUuid) {
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(firstContentUuid);
        ListItem contentItem2 = new ListItem();
        contentItem2.setUuid(secondContentUuid);
        List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);

        return new ContentList.Builder()
                .withUuid(UUID.fromString(listUuid))
                .withItems(content)
                .build();
    }

    private static ContentList getOutboundContentList(ContentList contentList) {
        contentList.addIds();
        contentList.addApiUrls(API_URL_PREFIX_CONTENT);
        contentList.removePrivateFields();
        return contentList;
    }

    private static final Map<String, String> templates = new HashMap<>();
    static {
        templates.put("http://www.ft.com/ontology/content/Article", "/content/{{id}}");
        templates.put("http://www.ft.com/ontology/content/ImageSet", "/content/{{id}}");
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(
                    new DocumentResource(
                            documentStoreService,
                            contentListValidator,
                            uuidValidator,
                            API_URL_PREFIX_CONTENT,
                            new ContentMapper(
                                    new IdentifierMapper(),
                                    new TypeResolver(),
                                    new BrandsMapper(),
                                    new StandoutMapper(),
                                    "localhost"),
                            new ContentBodyProcessingService(
                                    new ModelBodyXmlTransformer(
                                            new UriBuilder(templates)
                                    )
                            )
                    )
            )
            .addProvider(new ContextBackedApiUriGeneratorProvider(API_URL_PREFIX_CONTENT))
            .addProvider(DocumentStoreExceptionMapper.class)
            .build();

    @Before
    public void setup() {
        reset(documentStoreService);
        reset(contentListValidator);
        reset(uuidValidator);
        when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class))).thenReturn(DocumentWritten.created(listAsDocument));
    }

    //WRITE

    @Test
    public void shouldReturn201ForNewDocument() {
        ClientResponse clientResponse = writeDocument(uuidPath, listAsDocument);
        assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
        verify(documentStoreService).write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class));
    }

    @Test
    public void shouldReturn200ForUpdatedContent() {
        when(documentStoreService.write(eq(RESOURCE_TYPE), anyMapOf(String.class, Object.class))).thenReturn(DocumentWritten.updated(listAsDocument));

        ClientResponse clientResponse = writeDocument(uuidPath, listAsDocument);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn400OnWriteWhenUuidNotValid() {
        doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
        ClientResponse clientResponse = writeDocument(uuidPath, listAsDocument);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Invalid Uuid", clientResponse);
    }

    @Test
    public void shouldReturn503WhenCannotAccessExternalSystem() {
        when(documentStoreService.write(eq(RESOURCE_TYPE), any())).thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));

        ClientResponse clientResponse = writeDocument(uuidPath, listAsDocument);

        assertThat("", clientResponse, hasProperty("status", equalTo(503)));

    }

    //DELETE

    @Test
    public void shouldReturn200WhenDeletedSuccessfully() {
        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn200WhenDeletingNonExistentContentList() {
        doThrow(new DocumentNotFoundException(UUID.fromString(uuid))).when(documentStoreService).delete(eq(RESOURCE_TYPE), any(UUID.class));

        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn400OnDeleteWhenUuidNotValid() {
        doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Invalid Uuid", clientResponse);
    }

    @Test
    public void shouldReturn503OnDeleteWhenMongoIsntReachable() {
        doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).delete(eq(RESOURCE_TYPE), any(UUID.class));

        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }

    //READ
    @Test
    public void shouldReturn200WhenReadSuccessfully() {
        when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class))).thenReturn(listAsDocument);
        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final ContentList retrievedDocument = clientResponse.getEntity(ContentList.class);
        assertThat("inboundListAsDocument", retrievedDocument, equalTo(outboundList));
    }

    @Test
    public void shouldReturn404WhenContentNotFound() {
        when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class))).thenReturn(null);
        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        validateErrorMessage("Requested item does not exist", clientResponse);
    }

    @Test
    public void shouldReturn400OnReadWhenUuidNotValid() {
        doThrow(new ValidationException("Invalid Uuid")).when(uuidValidator).validate(anyString());
        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Invalid Uuid", clientResponse);
    }

    @Test
    public void shouldReturn503OnReadWhenMongoIsntReachable() {
        doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).findByUuid(eq(RESOURCE_TYPE), any(UUID.class));

        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }
    
    //FIND LIST BY CONCEPT AND TYPE
    @Test
    public void shouldReturn200ForDocumentFoundByConceptAndType() {
        String conceptID = "123";
        String type = "TopStories";
        String typeParam = "curatedTopStoriesFor";
        
        when(documentStoreService.findByConceptAndType(eq(RESOURCE_TYPE), eq(conceptID), eq(type))).thenReturn(listAsDocument);
        ClientResponse clientResponse = resources.client().resource("/lists")
                .queryParam(typeParam, conceptID)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final ContentList retrievedDocument = clientResponse.getEntity(ContentList.class);
        assertThat("documents were not the same", retrievedDocument, equalTo(outboundList));
    }
    
    @Test
    public void shouldReturn404ForDocumentNotFoundByConceptAndType() {
        String conceptID = "123";
        String type = "TopStories";
        String typeParam = "curatedTopStoriesFor";
        
        when(documentStoreService.findByConceptAndType(eq(RESOURCE_TYPE), eq(conceptID), eq(type))).thenReturn(null);
        ClientResponse clientResponse = resources.client().resource("/lists")
                .queryParam(typeParam, conceptID)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }

    @Test
    public void shouldReturn400ForNoQueryParameterSupplied() {
        String conceptID = "123";
        String invalidType = "TopStories";
        
        ClientResponse clientResponse = resources.client().resource("/lists")
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Expected at least one query parameter",  clientResponse);
    }
    
    @Test
    public void shouldReturn400ForNoValidQueryParameterSupplied() {
        String conceptID = "123";
        String invalidTypeParam = "invalidType";
        
        ClientResponse clientResponse = resources.client().resource("/lists")
                .queryParam(invalidTypeParam, conceptID)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Expected at least one query parameter of the form \"curated<listType>For\"", clientResponse);

    }

    //OTHER
    @Test
    public void shouldReturn405ForPost() {
        ClientResponse clientResponse = resources.client().resource(uuidPath)
                .post(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(405)));
    }


    private ClientResponse writeDocument(String writePath, Document document) {
        return resources.client()
                .resource(writePath)
                .entity(document, MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);
    }

    private void validateErrorMessage(String expectedErrorMessage, ClientResponse clientResponse) {
        final ErrorEntity responseBodyMessage = clientResponse.getEntity(ErrorEntity.class);
        assertThat("message", responseBodyMessage, hasProperty("message", equalTo(expectedErrorMessage)));
    }

}
