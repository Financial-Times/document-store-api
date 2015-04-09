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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentItem;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientResponse;

public class DocumentResourceContentListEndpointsTest {

    private static final String RESOURCE_TYPE = "lists";
    private String uuid;
    private String writePath;
    private ContentList contentList;

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
        String contentUuid1 = UUID.randomUUID().toString();
        String contentUuid2 = UUID.randomUUID().toString();
        writePath = "/lists/" + uuid;
        contentList = new ContentList();
        contentList.setUuid(uuid);
        ContentItem contentItem1 = new ContentItem();
        contentItem1.setUuid(contentUuid1);
        ContentItem contentItem2 = new ContentItem();
        contentItem2.setUuid(contentUuid2);
        List<ContentItem> content = ImmutableList.of(contentItem1, contentItem2);
        contentList.setContent(content);
        
        when(documentStoreService.write(eq(RESOURCE_TYPE), any(ContentList.class), any())).thenReturn(DocumentWritten.created(contentList));
    }
    
    //WRITE
    
    @Test
    public void shouldReturn201ForNewContent() {
        ClientResponse clientResponse = writeContentList(writePath, contentList);
        assertThat("response", clientResponse, hasProperty("status", equalTo(201)));
        verify(documentStoreService).write(eq(RESOURCE_TYPE), any(ContentList.class), any());
    }

    @Test
    public void shouldReturn200ForUpdatedContent() {
        when(documentStoreService.write(eq(RESOURCE_TYPE), any(ContentList.class), any())).thenReturn(DocumentWritten.updated(contentList));

        ClientResponse clientResponse = writeContentList(writePath, contentList);
        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    }

    @Test
    public void shouldReturn400WhenContentDocumentValidationFails() {
        doThrow(new ValidationException("Validation failed")).when(contentListDocumentValidator).validate(eq(uuid), any(ContentList.class));

        ClientResponse clientResponse = writeContentList(writePath, contentList);
        
        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
        validateErrorMessage("Validation failed", clientResponse);

    }    

    @Test
    public void shouldReturn503WhenCannotAccessExternalSystem() {   
        when(documentStoreService.write(eq(RESOURCE_TYPE), any(ContentList.class), any())).thenThrow(new ExternalSystemUnavailableException("Cannot connect to Mongo"));
        
        ClientResponse clientResponse = writeContentList(writePath, contentList);
        
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
    	doThrow(new ContentNotFoundException(UUID.fromString(uuid))).when(documentStoreService).delete(eq(RESOURCE_TYPE),any(UUID.class), any());
    	
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
        when(documentStoreService.findByUuid(eq(RESOURCE_TYPE), any(UUID.class), any())).thenReturn(contentList);
        ClientResponse clientResponse = resources.client().resource(writePath)
                .get(ClientResponse.class);

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final ContentList retrievedContentList = clientResponse.getEntity(ContentList.class);
        assertThat("contentList", retrievedContentList, equalTo(contentList));
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


    private ClientResponse writeContentList(String writePath, ContentList contentList) {
        return resources.client()
                .resource(writePath)
                .entity(contentList, MediaType.APPLICATION_JSON)
                .put(ClientResponse.class);
    }

    private void validateErrorMessage(String expectedErrorMessage, ClientResponse clientResponse) {
        final ErrorEntity responseBodyMessage = clientResponse.getEntity(ErrorEntity.class);
        assertThat("message", responseBodyMessage, hasProperty("message", equalTo(expectedErrorMessage)));
    }

}
