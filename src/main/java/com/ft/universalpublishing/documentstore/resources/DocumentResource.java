package com.ft.universalpublishing.documentstore.resources;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
import com.ft.universalpublishing.documentstore.model.Content;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.model.MainImage;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.ContentDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.ContentListDocumentValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;

@Path("/")
public class DocumentResource {

    private static final String CONTENT_COLLECTION = "content";

    private static final String LISTS_COLLECTION = "lists";

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
    
	private final DocumentStoreService documentStoreService;
	
	private ContentDocumentValidator contentDocumentValidator;
	private ContentListDocumentValidator contentListDocumentValidator;
	private UuidValidator uuidValidator;

    public DocumentResource(DocumentStoreService documentStoreService,
                            ContentDocumentValidator contentDocumentValidator,
                            ContentListDocumentValidator contentListDocumentValidator,
                            UuidValidator uuidValidator
                            ) {
    	this.documentStoreService = documentStoreService;
    	this.contentDocumentValidator = contentDocumentValidator;
    	this.contentListDocumentValidator = contentListDocumentValidator;
        this.uuidValidator = uuidValidator;
	}

	@GET
    @Timed
    @Path("/content/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Document getContentByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
		validateUuid(uuidString);
	    return findResourceByUuid(CONTENT_COLLECTION, uuidString, Content.class);
    }
    
    @GET
    @Timed
    @Path("/lists/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Document getListsByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        validateUuid(uuidString);
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
    public Response writeContent(@PathParam("uuidString") String uuidString, Map<String, Object> contentMap, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        Content content = convertMapToContent(contentMap); //TODO - remove once we have consistency of content internally and externally
        
        try {
            contentDocumentValidator.validate(uuidString, content);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
    	return writeDocument(CONTENT_COLLECTION, content, uriInfo, Content.class);
    
    }
    
    private Content convertMapToContent(Map<String, Object> contentMap) {
        // fix up the mismatches
        // 1) different field name for body
        String body = (String) contentMap.get("body");
        if (body != null) {
            contentMap.put("bodyXML", contentMap.get("body"));
        }
        // 2) mainImage is a String coming in and an object going out
        Object mainImage = contentMap.get("mainImage");
        if (mainImage != null && mainImage instanceof String) {
            String mainImageUuid = (String) mainImage;
            contentMap.put("mainImage", new MainImage(null, mainImageUuid));
        }
        // 3) brands are a list of objects coming in and a list of strings going out (but should be a list of objects going out too)
        Object rawBrands = contentMap.get("brands");
        if (rawBrands != null && rawBrands instanceof List<?>) {
            List rawList = (List) rawBrands;
            if (rawList.size() > 0) {
                Object firstBrand = rawList.get(0);
                if (firstBrand instanceof Map<?, ?>) {
                    SortedSet<String> brands = new TreeSet<String>();
                    List<Map<String, Object>> rawTypedList = (List<Map<String, Object>>) rawList;
                    for(Map<String, Object> rawBrand: rawTypedList) {
                        brands.add((String)rawBrand.get("id"));
                    }
                    contentMap.put("brands", brands);
                }
            }
        }   
        
        ObjectMapper m = new ObjectMapper();
        return m.convertValue(contentMap, Content.class);
    }


    @PUT
    @Timed
    @Path("/lists/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeLists(@PathParam("uuidString") String uuidString, ContentList contentList, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        
        try {
            contentListDocumentValidator.validate(uuidString, contentList);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
        return writeDocument(LISTS_COLLECTION, contentList, uriInfo, ContentList.class);
    
    }

    private <T extends Document> Response writeDocument(String resourceType, T document, UriInfo uriInfo, Class<T> documentClass) {
        try {
            final DocumentWritten written = 
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
        validateUuid(uuidString);
        return delete(CONTENT_COLLECTION, uuidString, Content.class);
    }
    
    @DELETE
    @Timed
    @Path("/lists/{uuidString}")
    public Response deleteList(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        return delete(LISTS_COLLECTION, uuidString, ContentList.class);
    }

    private void validateUuid(String uuidString) {
        try {
            uuidValidator.validate(uuidString);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
    }

    private <T extends Document> Response delete(String resourceType, String uuidString, Class<T> documentClass) {
        try {
            documentStoreService.delete(resourceType, UUID.fromString(uuidString), documentClass);
            return Response.noContent().build();
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("Service Unavailable").exception(esue);
        } catch (DocumentNotFoundException e){
            return Response.status(404).build();
        }
    }
}
