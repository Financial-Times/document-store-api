package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

import static com.ft.universalpublishing.documentstore.resources.DocumentResource.CHARSET_UTF_8;

@Path("/")
public class DocumentIDResource {

    private final MongoDocumentStoreService documentStoreService;

    public DocumentIDResource(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @GET
    @Path("/{collection}/__ids")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Response getIDsForCollectionAndAuthority(@PathParam("collection") String collection,
                                                          @QueryParam("includeSource") boolean includeSource) throws IOException {
        StreamingOutput streamingOutput = outputStream -> documentStoreService.findUUIDs(collection, includeSource, outputStream);
        return Response.ok().entity(streamingOutput).build();
    }
}
