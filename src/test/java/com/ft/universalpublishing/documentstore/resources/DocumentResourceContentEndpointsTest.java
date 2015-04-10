package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dropwizard.testing.junit.ResourceTestRule;

import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.Content;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.sun.jersey.api.client.ClientResponse;

public class DocumentResourceContentEndpointsTest {

    private static final String RESOURCE_TYPE = "content";
    private String uuid;
    private String writePath;
    private Content content;

    private final static DocumentStoreService documentStoreService = mock(DocumentStoreService.class);
    private final static ContentDocumentValidator contentDocumentValidator= mock(ContentDocumentValidator.class);
    private final static ContentListDocumentValidator contentListDocumentValidator= mock(ContentListDocumentValidator.class);
    
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new DocumentResource(documentStoreService, contentDocumentValidator, contentListDocumentValidator))
        .build();

    @Before
    public void setup() {
        reset(documentStoreService);
        reset(contentDocumentValidator);
        reset(contentListDocumentValidator);
        uuid = UUID.randomUUID().toString();
        writePath = "/content/" + uuid;
        Date lastPublicationDate = new Date();
        content = new Content();
        content.setUuid(uuid);
        content.setTitle("Here's the news");
        content.setBodyXml("xmlBody");
        content.setPublishedDate(lastPublicationDate);
        when(documentStoreService.write(eq(RESOURCE_TYPE), any(Content.class), any())).thenReturn(DocumentWritten.created(content));
    }
    
    //WRITE
    
    @Test
    public void shouldReturn201ForNewContent() {
        ClientResponse clientResponse = writeContent(writePath, content);
        assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
        verify(documentStoreService).write(eq(RESOURCE_TYPE), any(Content.class), any());
    }

    @Test
    public void shouldReturn200ForUpdatedContent() {
        when(documentStoreService.write(eq(RESOURCE_TYPE), any(Content.class), any())).thenReturn(DocumentWritten.updated(content));

        ClientResponse clientResponse = writeContent(writePath, content);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn400WhenContentDocumentValidationFails() {
        doThrow(new ValidationException("Validation failed")).when(contentDocumentValidator).validate(eq(uuid), any(Content.class));

        ClientResponse clientResponse = writeContent(writePath, content);
        
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Validation failed", clientResponse);

    }    

    @Test
    public void shouldReturn503WhenCannotAccessExternalSystem() {   
        when(documentStoreService.write(eq(RESOURCE_TYPE), any(Content.class), any())).thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));
        
        ClientResponse clientResponse = writeContent(writePath, content);
        
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
    public void shouldReturn404WhenDeletingNonExistentContent(){
    	doThrow(new ContentNotFoundException(UUID.randomUUID())).when(documentStoreService).delete(eq(RESOURCE_TYPE),any(UUID.class), any());
    	
    	ClientResponse clientResponse = resources.client().resource(writePath)
    			.delete(ClientResponse.class);
    	
    	assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
    }
    
    @Test
    public void shouldReturn503OnDeleteWhenMongoIsntReachable(){
    	doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).delete(eq(RESOURCE_TYPE),any(UUID.class), any());
    	
    	ClientResponse clientResponse = resources.client().resource(writePath)
    			.delete(ClientResponse.class);
    	
    	assertThat("response", clientResponse, hasProperty("status", equalTo(503)));
    }
	
    //READ
    @Test
    //TODO make sure Date comes back as a Date!
    public void shouldReturn200WhenReadSuccessfully() {
        when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class), any())).thenReturn(content);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final Content retrievedContent = clientResponse.getEntity(Content.class);
        assertThat(RESOURCE_TYPE, retrievedContent, equalTo(content));
    }
    
    @Test
    public void shouldReturn404WhenContentNotFound() {
        when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class), any())).thenReturn(null);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(404)));
        validateErrorMessage("Requested item does not exist", clientResponse);
    }
    
    @Test
    public void shouldReturn503OnReadWhenMongoIsntReachable(){
        doThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo")).when(documentStoreService).findByUuid(eq(RESOURCE_TYPE),any(UUID.class), any());
        
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


    private ClientResponse writeContent(String writePath, Content content) {
        return resources.client()
                .resource(writePath)
                .entity(content, MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);
    }

    private void validateErrorMessage(String expectedErrorMessage, ClientResponse clientResponse) {
        final ErrorEntity responseBodyMessage = clientResponse.getEntity(ErrorEntity.class);
        assertThat("message", responseBodyMessage, hasProperty("message", equalTo(expectedErrorMessage)));
    }

}
