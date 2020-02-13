package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.CLIENT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_GET;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static javax.ws.rs.core.Response.ok;
import static org.slf4j.MDC.get;

@Api(tags = {"collections"})
@Path("/")
public class DocumentIDResource {

    private final MongoDocumentStoreService documentStoreService;

    public DocumentIDResource(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @ApiOperation(
            value = "List all available authority identifiers that are allowed to write in the current collection"
    )
    @GET
    @Path("/{collection}/__ids")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public final Response getIDsForCollectionAndAuthority(@PathParam("collection") String collection,
                                                          @QueryParam("includeSource") boolean includeSource) {
        StreamingOutput streamingOutput = outputStream -> documentStoreService.findUUIDs(collection, includeSource, outputStream);
        Response response = ok().entity(streamingOutput).build();
        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "getIDsForCollectionAndAuthority")
                .withResponse(response)
                .withTransactionId(get(TRANSACTION_ID)).withField(CLIENT, response.getClass().getCanonicalName())
                .withField(METHOD, METHOD_GET)
                .build().logInfo();

        return response;
    }
}
