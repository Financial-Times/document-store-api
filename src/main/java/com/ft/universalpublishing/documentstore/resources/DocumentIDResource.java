package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Api(tags = {"collections"})
@Path("/")
public class DocumentIDResource {

  private final MongoDocumentStoreService documentStoreService;

  private static final Logger LOG = LoggerFactory.getLogger(DocumentIDResource.class);

  public DocumentIDResource(MongoDocumentStoreService documentStoreService) {
    this.documentStoreService = documentStoreService;
  }

  @ApiOperation(
      value =
          "List all available authority identifiers that are allowed to write in the current collection")
  @GET
  @Path("/{collection}/__ids")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public final Response getIDsForCollectionAndAuthority(
      @PathParam("collection") String collection,
      @QueryParam("includeSource") boolean includeSource) {
    LOG.info().message("Collection streaming API call detected");
    StreamingOutput streamingOutput =
        outputStream -> documentStoreService.findUUIDs(collection, includeSource, outputStream);
    return Response.ok().entity(streamingOutput).build();
  }
}
