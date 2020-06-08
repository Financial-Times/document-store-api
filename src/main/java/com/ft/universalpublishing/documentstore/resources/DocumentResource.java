package com.ft.universalpublishing.documentstore.resources;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.JsonLogger;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Api(tags = {"collections"})
@Path("/")
public class DocumentResource {

  protected static final String CHARSET_UTF_8 = ";charset=utf-8";
  private static final String appName = "document-store-api";

  private Map<Pair<String, Operation>, HandlerChain> collections;
  private final Logger LOGGER = LoggerFactory.getLogger(DocumentResource.class);

  public DocumentResource(Map<Pair<String, Operation>, HandlerChain> collections) {
    this.collections = collections;
  }

  @ApiOperation(value = "Get documents from the specified collection per content type UUID")
  @GET
  @Timed
  @Path("/{collection}/{uuidString}")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuid(
      @PathParam("uuidString") String uuidString, @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUuids(uuidString);
    context.setCollection(collection);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_BY_ID);
    return handlerChain.execute(context);
  }

  @ApiOperation(value = "Search and filter documents from the specified list collection")
  @GET
  @Timed
  @Path("search/{collection}")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object searchCollection(
      @PathParam("collection") String collection,
      @QueryParam("conceptUUID") String conceptUUID,
      @QueryParam("listType") String listType,
      @QueryParam("searchTerm") String searchTerm,
      @QueryParam("webUrl") String webUrl,
      @QueryParam("standfirst") String standfirst) {
    Context context = new Context();
    context.setCollection(collection);
    context.setConceptUUID(conceptUUID);
    context.setListType(listType);
    context.setSearchTerm(searchTerm);
    context.setWebUrl(webUrl);
    context.setStandfirst(standfirst);

    HandlerChain handlerChain = getHandlerChain(collection, Operation.SEARCH);
    return handlerChain.execute(context);
  }

  @GET
  @Timed
  @Path("/{collection}")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuids(
      @javax.ws.rs.core.Context HttpHeaders httpHeaders,
      @javax.ws.rs.core.Context UriInfo uriInfo,
      @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUriInfo(uriInfo);
    context.setHttpHeaders(httpHeaders);
    context.setCollection(collection);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_FILTERED);
    return handlerChain.execute(context);
  }

  @ApiOperation(
      value = "Get documents from the specified collection per list of content type UUIDs")
  @POST
  @Timed
  @Path("/{collection}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuids(
      @javax.ws.rs.core.Context HttpHeaders httpHeaders,
      List<String> uuidList,
      @javax.ws.rs.core.Context UriInfo uriInfo,
      @PathParam("collection") String collection,
      @QueryParam("mget") boolean mget) {
    if (!mget) {
      throw ClientError.status(400)
          .exception(
              new IllegalArgumentException(
                  "This is an endpoint for retrieving data, not writing. You must supply query parameter ?mget=true"));
    }
    if (uuidList == null) {
      throw ClientError.status(400)
          .exception(
              new IllegalArgumentException(
                  "Incorrect format of request body. It's not an array of strings."));
    }

    Context context = new Context();
    context.setUriInfo(uriInfo);
    context.setHttpHeaders(httpHeaders);
    context.setCollection(collection);
    context.setUuids(uuidList);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_MULTIPLE_FILTERED);
    return handlerChain.execute(context);
  }

  @ApiOperation(value = "Add/update a document identified by UUID")
  @PUT
  @Timed
  @Path("/{collection}/{uuidString}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Object writeInCollection(
      @PathParam("uuidString") String uuidString,
      Map<String, Object> contentMap,
      @javax.ws.rs.core.Context UriInfo uriInfo,
      @PathParam("collection") String collection) {

    Context context = new Context();
    context.setCollection(collection);
    context.setUuids(uuidString);
    context.setContentMap(contentMap);
    context.setUriInfo(uriInfo);

    try {
      final JsonLogger jsonLogger = LOGGER.info();
      HandlerChain handlerChain = getHandlerChain(collection, Operation.ADD);
      Object result = handlerChain.execute(context);
      jsonLogger
          .field("@time", ISO_INSTANT.format(Instant.now()))
          .field("event", "SaveDocStore")
          .field("collection", collection)
          .field("monitoring_event", "true")
          .field("service_name", appName)
          .field("content_type", contentMap.get("type"))
          .field("uuid", uuidString)
          .message("Successfully saved")
          .log();
      return result;

    } catch (WebApplicationClientException ex) {
      final JsonLogger jsonErrorLogger = LOGGER.error();
      jsonErrorLogger
          .field("uuid", uuidString)
          .field("@time", ISO_INSTANT.format(Instant.now()))
          .field("event", "SaveDocStore")
          .field("monitoring_event", "true")
          .field("collection", collection)
          .field("service_name", appName)
          .message("Error: " + ex.getMessage())
          .log();
      throw ex;
    }
  }

  @ApiOperation(value = "Delete a document identified by UUID")
  @DELETE
  @Timed
  @Path("/{collection}/{uuidString}")
  public Object deleteFromCollection(
      @PathParam("uuidString") String uuidString,
      @javax.ws.rs.core.Context UriInfo uriInfo,
      @PathParam("collection") String collection) {

    Context context = new Context();
    context.setUuids(uuidString);
    context.setCollection(collection);
    context.setUriInfo(uriInfo);

    try {
      final JsonLogger jsonLogger = LOGGER.info();
      HandlerChain handlerChain = getHandlerChain(collection, Operation.REMOVE);
      Object result = handlerChain.execute(context);
      jsonLogger
          .field("uuid", uuidString)
          .field("@time", ISO_INSTANT.format(Instant.now()))
          .field("event", "SaveDocStore")
          .field("collection", collection)
          .field("monitoring_event", "true")
          .field("service_name", appName)
          .message("Successfully deleted")
          .log();
      return result;

    } catch (WebApplicationClientException e) {
      final JsonLogger jsonErrorLogger = LOGGER.error();
      jsonErrorLogger
          .field("uuid", uuidString)
          .field("@time", ISO_INSTANT.format(Instant.now()))
          .field("event", "SaveDocStore")
          .field("monitoring_event", "true")
          .field("collection", collection)
          .field("service_name", appName)
          .message("Error: " + e.getMessage())
          .log();
      throw e;
    }
  }

  protected HandlerChain getHandlerChain(String collection, Operation Operation) {
    Pair<String, Operation> pair = new Pair<>(collection, Operation);
    if (collections.containsKey(pair)) {
      return collections.get(pair);
    }
    collections
        .keySet()
        .forEach(
            keyPair -> {
              if (keyPair.getKey().equals(collection)) {
                // This method is not allowed
                throw ClientError.status(405).exception();
              }
            });
    throw ClientError.status(400).exception();
  }
}
