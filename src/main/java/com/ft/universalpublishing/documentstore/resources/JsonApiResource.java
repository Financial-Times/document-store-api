package com.ft.universalpublishing.documentstore.resources;


import java.util.UUID;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.content.model.Content;
import com.ft.universalpublishing.documentstore.exception.ContentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.mongo.MongoContentReader;
import com.ft.universalpublishing.documentstore.mongo.MongoContentWriter;
import com.google.common.base.Optional;
import com.mongodb.MongoClient;

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

@Path("/")
public class JsonApiResource {

    private static final String CHARSET_UTF_8 = ";charset=utf-8";
	private final MongoContentWriter contentWriter;
	private final MongoContentReader contentReader;

    public JsonApiResource(MongoContentWriter contentWriter, MongoContentReader contentReader) {
    	this.contentWriter = contentWriter;
    	this.contentReader = contentReader;
	}

	@GET
    @Timed
    @Path("/content/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Content getByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
    	
		 try {
	            final Optional<Content> foundContent = contentReader.findByUuid(UUID.fromString(uuidString));
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
    public Response write(@PathParam("uuidString") String uuidString, Content document, @Context UriInfo uriInfo) {
    	
    	validateInputs(uuidString, document);
        try {
            final com.ft.universalpublishing.documentstore.write.ContentWritten written = contentWriter.write(document);
            final Response response;
            switch (written.getMode()) {
                case Created:
                    response = Response.created(uriInfo.getRequestUri()).build();
                    break;
                case Updated:
                    response = Response.ok(written.getContent()).build();
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
//    public Response delete(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
//        try {
//        	contentWriter.delete(UUID.fromString(uuidString));
//            return Response.noContent().build();
//        } catch (ExternalSystemUnavailableException esue) {
//            throw ServerError.status(503).error("Service Unavailable").exception(esue);
//        } catch (ContentNotFoundException e){
//            return Response.ok().build();
//        }
//    }
    
    private void validateInputs(String uuidString, Content document) {

        if (document == null) {
            throw ClientError.status(400).error("some content must be submitted").exception();
        }
        if (document.getUuid() == null) {
            throw ClientError.status(400).error("submitted content must provide a uuid").exception();
        }
        if (document.getTitle() == null || document.getTitle().isEmpty()) {
            throw ClientError.status(400).error("submitted content must provide a non-empty title").exception();
        }
        if (document.getPublishedDate() == null ) {
            throw ClientError.status(400).error("submitted content must provide a non-empty publishedDate").exception();
        }
        if (!uuidString.equals(document.getUuid())) {
            String message = String.format("uuid in path %s is not equal to uuid in submitted content %s", uuidString, document.getUuid());
            throw ClientError.status(400).error(message).exception();

        }
    }
}
