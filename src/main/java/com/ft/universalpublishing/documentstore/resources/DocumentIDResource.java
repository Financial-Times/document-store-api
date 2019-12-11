package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.CLIENT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static javax.ws.rs.core.Response.ok;
import static org.slf4j.MDC.get;

@Path("/")
public class DocumentIDResource {

    private final MongoDocumentStoreService documentStoreService;

    public DocumentIDResource(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @GET
    @Path("/{collection}/__ids")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public final Response getIDsForCollectionAndAuthority(@PathParam("collection") String collection,
                                                          @QueryParam("includeSource") boolean includeSource) throws IOException {
        StreamingOutput streamingOutput = outputStream -> documentStoreService.findUUIDs(collection, includeSource, outputStream);
        Response response = ok().entity(streamingOutput).build();
        new FluentLoggingWrapper().withClassName(this.getClass().getCanonicalName())
                .withMetodName("getIDsForCollectionAndAuthority").withResponse(response)
                .withTransactionId(get(TRANSACTION_ID)).withField(CLIENT, response.getClass().getCanonicalName())
                .withField(METHOD, "GET")
                .build().logInfo();

        return response;
    }
}
