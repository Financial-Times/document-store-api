package com.ft.universalpublishing.documentstore.resources;


import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.Content;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.google.common.base.Optional;

@Path("/")
public class DocumentResource {

    private static final String CONTENT_COLLECTION = "content";

    private static final String LISTS_COLLECTION = "lists";

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    
	private final DocumentStoreService documentStoreService;
	
	private ContentDocumentValidator contentDocumentValidator;
	private ContentListDocumentValidator contentListDocumentValidator;

    public DocumentResource(DocumentStoreService documentStoreService, ContentDocumentValidator contentDocumentValidator, ContentListDocumentValidator contentListDocumentValidator) {
    	this.documentStoreService = documentStoreService;
    	this.contentDocumentValidator = contentDocumentValidator;
    	this.contentListDocumentValidator = contentListDocumentValidator;
	}

	@GET
    @Timed
    @Path("/content/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Document getContentByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
		//TODO validate uuid 
	    return findResourceByUuid(CONTENT_COLLECTION, uuidString, Content.class);
    }
    
    @GET
    @Timed
    @Path("/lists/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Document getListsByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        //TODO validate uuid 
        return findResourceByUuid(LISTS_COLLECTION, uuidString, ContentList.class);
    }

    private <T extends Document> T findResourceByUuid(String resourceType, String uuidString, Class<T> documentClass) {
        try {
            final T foundDocument = documentStoreService.findByUuid(resourceType, UUID.fromString(uuidString), documentClass);
            if (foundDocument!= null) {
                return foundDocument;
            } else {
                throw ClientError.status(404).error("Requested item does not exist").exception();
            }
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("upstream system unavailable").exception(esue);
        }
    }
    
    @PUT
    @Timed
    @Path("/content/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeContent(@PathParam("uuidString") String uuidString, Content content, @Context UriInfo uriInfo) {

        try {
            contentDocumentValidator.validate(uuidString, content);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
    	return writeDocument(CONTENT_COLLECTION, content, uriInfo, Content.class);
    
    }
    
    @PUT
    @Timed
    @Path("/lists/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeLists(@PathParam("uuidString") String uuidString, ContentList contentList, @Context UriInfo uriInfo) {
        try {
            contentListDocumentValidator.validate(uuidString, contentList);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
        return writeDocument(LISTS_COLLECTION, contentList, uriInfo, ContentList.class);
    
    }

    private <T extends Document> Response writeDocument(String resourceType, T document, UriInfo uriInfo, Class<T> documentClass) {
        try {
            final com.ft.universalpublishing.documentstore.write.DocumentWritten written = 
                    documentStoreService.write(resourceType, document, documentClass);
            final Response response;
            switch (written.getMode()) {
                case Created:
                    response = Response.created(uriInfo.getRequestUri()).build();
                    break;
                case Updated:
                    response = Response.ok(written.getDocument()).build();
                    break;
                default:
                    throw new IllegalStateException("unknown write mode " + written.getMode());
            }
            return response;
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("Service Unavailable").exception(esue);
        }
    }
    
    @DELETE
    @Timed
    @Path("/content/{uuidString}")
    public Response deleteContent(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        return delete(CONTENT_COLLECTION, uuidString, Content.class);
    }
    
    @DELETE
    @Timed
    @Path("/lists/{uuidString}")
    public Response deleteList(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        return delete(LISTS_COLLECTION, uuidString, ContentList.class);
    }

    private <T extends Document> Response delete(String resourceType, String uuidString, Class<T> documentClass) {
        try {
            documentStoreService.delete(resourceType, UUID.fromString(uuidString), documentClass);
            return Response.noContent().build();
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("Service Unavailable").exception(esue);
        } catch (ContentNotFoundException e){
            return Response.status(404).build();
        }
    }
}
