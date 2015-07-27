package com.ft.universalpublishing.documentstore.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.mongodb.DBObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class ContentReadResource extends AbstractResource {

    public ContentReadResource(final DocumentStoreService documentStoreService,
                               final UuidValidator uuidValidator) {
        super(documentStoreService, uuidValidator);
    }

    @GET
    @Timed
    @Path("/content-read/{uuid}")
    @Produces(APPLICATION_JSON + CHARSET_UTF_8)
    public final com.ft.universalpublishing.documentstore.model.Content getContentByUuid(final @PathParam("uuid") String uuid, final @Context HttpHeaders headers) {
        validateUuid(uuid);
        DBObject dbObject = findResourceByUuid(CONTENT_COLLECTION, uuid);
        return map(dbObject);
    }

    private com.ft.universalpublishing.documentstore.model.Content map(DBObject obj) {
        throw new RuntimeException("Not implemented");
    }
}
