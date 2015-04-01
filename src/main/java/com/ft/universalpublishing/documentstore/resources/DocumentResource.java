package com.ft.universalpublishing.documentstore.resources;


import java.util.LinkedHashMap;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.model.Content;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.google.common.base.Optional;

@Path("/")
public class DocumentResource {

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    
	private final DocumentStoreService documentStoreService;
	
	private ContentDocumentValidator contentDocumentValidator = new ContentDocumentValidator();
	private ContentListDocumentValidator contentListDocumentValidator = new ContentListDocumentValidator();

    public DocumentResource(DocumentStoreService documentStoreService) {
    	this.documentStoreService = documentStoreService;

	}

	@GET
    @Timed
    @Path("/content/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Map<String, Object> getContentByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
		 return findResourceByUuid("content", uuidString);
    }
    
    @GET
    @Timed
    @Path("/lists/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Map<String, Object> getListsByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        return findResourceByUuid("lists", uuidString);
    }

    private Map<String, Object> findResourceByUuid(String resourceType, String uuidString) {
        try {
            final Optional<Map<String, Object>> foundContent = documentStoreService.findByUuid(resourceType, UUID.fromString(uuidString));
            if (foundContent.isPresent()) {
                return foundContent.get();
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

    	contentDocumentValidator.validate(uuidString, content);
    	return writeDocument("content", content, uriInfo);
    
    }
    
    @PUT
    @Timed
    @Path("/lists/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeLists(@PathParam("uuidString") String uuidString, ContentList contentList, @Context UriInfo uriInfo) {

        contentListDocumentValidator.validate(uuidString, contentList);
        return writeDocument("lists", contentList, uriInfo);
    
    }

    private Response writeDocument(String resourceType, Object document, UriInfo uriInfo) {
        Map<String, Object> documentAsMap = convertToMap(document);

        try {
            final com.ft.universalpublishing.documentstore.write.DocumentWritten written = documentStoreService.write(resourceType, documentAsMap);
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
        return delete("content", uuidString);
    }
    
    @DELETE
    @Timed
    @Path("/lists/{uuidString}")
    public Response deleteList(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        return delete("content", uuidString);
    }

    private Response delete(String resourceType, String uuidString) {
        try {
            documentStoreService.delete(uuidString, UUID.fromString(uuidString));
            return Response.noContent().build();
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("Service Unavailable").exception(esue);
        } catch (ContentNotFoundException e){
            return Response.ok().build();
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object object) {
        ObjectMapper m = new ObjectMapper();
        return m.convertValue(object, Map.class);
    }
}
