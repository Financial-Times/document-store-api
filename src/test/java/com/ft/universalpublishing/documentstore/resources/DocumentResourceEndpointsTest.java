package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.Content;
import com.ft.universalpublishing.documentstore.model.ListItem;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.DocumentValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DocumentResourceEndpointsTest {

    private String uuid;
    private String resourceType;
    private Document document;
    private String writePath;
    private Class<? extends Document> documentClass;
    private DocumentValidator documentValidator;

    private final static DocumentStoreService documentStoreService = mock(DocumentStoreService.class);
    private final static ContentDocumentValidator contentDocumentValidator= mock(ContentDocumentValidator.class);
    private final static ContentListDocumentValidator contentListDocumentValidator= mock(ContentListDocumentValidator.class);
    private final static UuidValidator uuidValidator= mock(UuidValidator.class);

    public DocumentResourceEndpointsTest(String resourceType, Document document, 
            String uuid, Class<? extends Document> documentClass, DocumentValidator documentValidator) {
        this.resourceType = resourceType;
        this.document = document;
        this.uuid = uuid;
        this.documentClass = documentClass;
        this.documentValidator = documentValidator;
        this.writePath = "/" + resourceType + "/" + uuid;
    }
    
    @Parameters
    public static Collection<Object[]> documents() {  
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        return Arrays.asList(new Object[][] {{"content", getContent(uuid1), uuid1, Content.class, contentDocumentValidator}, 
                                        {"lists", getContentList(uuid2), uuid2, ContentList.class, contentListDocumentValidator}});

    }
    
    private static Document getContent(String uuid) {
        Date lastPublicationDate = new Date();
        Content content = new Content();
        content.setUuid(uuid);
        content.setTitle("Here's the news");
        content.setBodyXML("xmlBody");
        content.setPublishedDate(lastPublicationDate);
        return content;
    }

    private static Document getContentList(String uuid) {
        String contentUuid1 = UUID.randomUUID().toString();
        String contentUuid2 = UUID.randomUUID().toString();
        ContentList contentList = new ContentList();
        contentList.setUuid(uuid);
        ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(contentUuid1);
        ListItem contentItem2 = new ListItem();
        contentItem2.setUuid(contentUuid2);
        List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);
        contentList.setItems(content);
        return contentList;
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new DocumentResource(documentStoreService, contentDocumentValidator, contentListDocumentValidator, uuidValidator))
        .build();

    @Before
    public void setup() {
        reset(documentStoreService);
        reset(contentDocumentValidator);
        reset(contentListDocumentValidator);      
        when(documentStoreService.write(eq(resourceType), any(Document.class), any())).thenReturn(DocumentWritten.created(document));
    }
    
    //WRITE
    
    @Test
    public void shouldReturn201ForNewContent() {
        ClientResponse clientResponse = writeDocument(writePath, document);
        assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
        verify(documentStoreService).write(eq(resourceType), any(Document.class), any());
    }

    @Test
    public void shouldReturn200ForUpdatedContent() {
        when(documentStoreService.write(eq(resourceType), any(Document.class), any())).thenReturn(DocumentWritten.updated(document));

        ClientResponse clientResponse = writeDocument(writePath, document);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn400WhenContentDocumentValidationFails() {
        doThrow(new ValidationException("Validation failed")).when(documentValidator).validate(eq(uuid), any(Document.class));

        ClientResponse clientResponse = writeDocument(writePath, document);
        
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Validation failed", clientResponse);

    }    

    @Test
    public void shouldReturn503WhenCannotAccessExternalSystem() {   
        when(documentStoreService.write(eq(resourceType), any(ContentList.class), any())).thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));
        
        ClientResponse clientResponse = writeDocument(writePath, document);
        
        assertThat("", clientResponse, hasProperty("status", equalTo(503)));
        
    }
    
    //DELETE

    @Test
    public void shouldReturn204WhenDeletedSuccessfully(){
        ClientResponse clientResponse = resources.client().resource(writePath)
                .delete(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(204)));
    }
    
    @Test
    public void shouldReturn404WhenDeletingNonExistentContentList(){
    	doThrow(new ContentNotFoundException(UUID.fromString(uuid))).when(documentStoreService).delete(eq(resourceType),any(UUID.class), any());
    	
    	ClientResponse clientResponse = resources.client().resource(writePath)
    			.delete(ClientResponse.class);
    	
    	assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }
    
    @Test
    public void shouldReturn503OnDeleteWhenMongoIsntReachable(){
    	doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).delete(eq(resourceType),any(UUID.class), any());
    	
    	ClientResponse clientResponse = resources.client().resource(writePath)
    			.delete(ClientResponse.class);
    	
    	assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }
	
    //READ
    @Test
    public void shouldReturn200WhenReadSuccessfully() {
        when(documentStoreService.findByUuid(eq(resourceType), any(UUID.class), any())).thenReturn(document);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final Document retrievedDocument = clientResponse.getEntity(documentClass);
        assertThat("document", retrievedDocument, equalTo(document));
    }
    
    @Test
    public void shouldReturn404WhenContentNotFound() {
        when(documentStoreService.findByUuid(eq(resourceType), any(UUID.class), any())).thenReturn(null);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        validateErrorMessage("Requested item does not exist", clientResponse);
    }
    
    @Test
    public void shouldReturn503OnReadWhenMongoIsntReachable(){
        doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).findByUuid(eq(resourceType),any(UUID.class), any());
        
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);
        
        assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }
    
    //OTHER
    @Test
    public void shouldReturn405ForPost(){
        ClientResponse clientResponse = resources.client().resource(writePath)
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
