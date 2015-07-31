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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.ContentMapper;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.model.IdentifierMapper;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;

@Path("/")
public class DocumentResource {

    protected static final String CHARSET_UTF_8 = ";charset=utf-8";
    public static final String CONTENT_COLLECTION = "content";
    public static final String LISTS_COLLECTION = "lists";
	
	private ContentListDocumentValidator contentListDocumentValidator;
    private DocumentStoreService documentStoreService;
    private UuidValidator uuidValidator;
    private String apiPath;
    private final ContentMapper contentMapper;

    public DocumentResource(DocumentStoreService documentStoreService,
                            ContentListDocumentValidator contentListDocumentValidator,
                            UuidValidator uuidValidator,
                            String apiPath,
                            final ContentMapper contentMapper) {
        this.documentStoreService = documentStoreService;
        this.uuidValidator = uuidValidator;
    	this.contentListDocumentValidator = contentListDocumentValidator;
        this.apiPath = apiPath;
        this.contentMapper = contentMapper;
    }

	@GET
    @Timed
    @Path("/content/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Map<String, Object> getContentByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
		validateUuid(uuidString);
	    return findResourceByUuid(CONTENT_COLLECTION, uuidString);
    }

    @GET
    @Timed
    @Path("/content-read/{uuid}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final com.ft.universalpublishing.documentstore.model.read.Content getContentReadByUuid(@PathParam("uuid") String uuid) {
        validateUuid(uuid);
        final Map<String, Object> resource = findResourceByUuid(CONTENT_COLLECTION, uuid);
        final Content content = new ObjectMapper().convertValue(resource, Content.class);
        return contentMapper.map(content);
    }
    
    @GET
    @Timed
    @Path("/lists/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final ContentList getListsByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        validateUuid(uuidString);
        Map<String, Object> contentMap = findResourceByUuid(LISTS_COLLECTION, uuidString);
        try {
            return convertToContentList(contentMap);
        } catch (IllegalArgumentException e) {
            throw ClientError.status(500).error(e.getMessage()).exception();
        }
    }

    protected ContentList convertToContentList(Map<String, Object> contentMap) {
        ContentList contentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
        contentList.addIds();
        contentList.addApiUrls(apiPath);
        contentList.removePrivateFields();
        return contentList;
    }

    @PUT
    @Timed
    @Path("/content/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeContent(@PathParam("uuidString") String uuidString, Map<String, Object> contentMap, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
    	return writeDocument(CONTENT_COLLECTION, contentMap, uriInfo);
    
    }


    @PUT
    @Timed
    @Path("/lists/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeLists(@PathParam("uuidString") String uuidString, Map<String, Object> contentMap, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        try {
            ContentList contentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
            contentListDocumentValidator.validate(uuidString, contentList);
        } catch (ValidationException | IllegalArgumentException e) {
            throw ClientError.status(400).error(e.getMessage()).exception();
        }
        return writeDocument(LISTS_COLLECTION, contentMap, uriInfo);
    
    }

    private <T extends Document> Response writeDocument(String resourceType, Map<String, Object> content, UriInfo uriInfo) {
        try {
            final DocumentWritten written = 
                    documentStoreService.write(resourceType, content);
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
        validateUuid(uuidString);
        return delete(CONTENT_COLLECTION, uuidString);
    }
    
    @DELETE
    @Timed
    @Path("/lists/{uuidString}")
    public Response deleteList(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        return delete(LISTS_COLLECTION, uuidString);
    }

    private Response delete(String resourceType, String uuidString) {
        try {
            documentStoreService.delete(resourceType, UUID.fromString(uuidString));
            return Response.noContent().build();
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("Service Unavailable").exception(esue);
        } catch (DocumentNotFoundException e){
            return Response.status(404).build();
        }
    }

    protected void validateUuid(String uuidString) {
        try {
            uuidValidator.validate(uuidString);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
    }


    protected Map<String, Object> findResourceByUuid(final String resourceType, final String uuid) {
        try {
            final Map<String, Object> foundDocument = documentStoreService.findByUuid(resourceType, UUID.fromString(uuid));
            if (foundDocument!= null) {
                foundDocument.remove("_id");
                return foundDocument;
            } else {
                throw ClientError.status(404).error("Requested item does not exist").exception();
            }
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("upstream system unavailable").exception(esue);
        }
    }
}
