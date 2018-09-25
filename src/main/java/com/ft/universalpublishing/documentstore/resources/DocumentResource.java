package com.ft.universalpublishing.documentstore.resources;

import com.codahale.metrics.annotation.Timed;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.JsonLogger;
import com.savoirtech.logging.slf4j.json.logger.Logger;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.IDStreamingException;
import com.ft.universalpublishing.documentstore.exception.MethodeNotAllowedException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import jdk.net.SocketFlow;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.parsedExcessDays;

@Path("/")
public class DocumentResource {

  protected static final String CHARSET_UTF_8 = ";charset=utf-8";

  private Map<Pair<String, Operation>, HandlerChain> collections;
  private final Logger LOGGER = LoggerFactory.getLogger(DocumentResource.class);
  private final JsonLogger jsonLogger = LOGGER.info();


  public DocumentResource(Map<Pair<String, Operation>, HandlerChain> collections) {
    this.collections = collections;
  }

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

  @GET
  @Timed
  @Path("/{collection}")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuids(
          @javax.ws.rs.core.Context HttpHeaders httpHeaders,
          @javax.ws.rs.core.Context UriInfo uriInfo, @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUriInfo(uriInfo);
    context.setHttpHeaders(httpHeaders);
    context.setCollection(collection);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_FILTERED);
    return handlerChain.execute(context);
  }

  @POST
  @Timed
  @Path("/{collection}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Object getFromCollectionByUuids(@javax.ws.rs.core.Context HttpHeaders httpHeaders,
                                               List<String> uuidList,
                                               @javax.ws.rs.core.Context UriInfo uriInfo,
                                               @PathParam("collection") String collection,
                                               @QueryParam("mget") boolean mget) {
    if (!mget) {
      throw ClientError.status(400).exception(new IllegalArgumentException("This is an endpoint for retrieving data, not writing. You must supply query parameter ?mget=true"));
    }
    if (uuidList == null) {
      throw ClientError.status(400).exception(new IllegalArgumentException("Incorrect format of request body. It's not an array of strings."));
    }

    Context context = new Context();
    context.setUriInfo(uriInfo);
    context.setHttpHeaders(httpHeaders);
    context.setCollection(collection);
    context.setUuids(uuidList);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_MULTIPLE_FILTERED);
    return handlerChain.execute(context);
  }

  @PUT
  @Timed
  @Path("/{collection}/{uuidString}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Object writeInCollection(@PathParam("uuidString") String uuidString,
                                  Map<String, Object> contentMap, @javax.ws.rs.core.Context UriInfo uriInfo,
                                  @PathParam("collection") String collection) {

    Context context = new Context();
    context.setCollection(collection);
    context.setUuids(uuidString);
    context.setContentMap(contentMap);
    context.setUriInfo(uriInfo);

    try {

      HandlerChain handlerChain = getHandlerChain(collection, Operation.ADD);
      Object execute = handlerChain.execute(context);
      jsonLogger.field("@time", ISO_INSTANT.format(Instant.now()))
              .field("event", "SaveDocStore")
              .field("collection", collection)
              .field("monitoring_event", "true")
              .field("service_name", "document-store-api")
              .message("Successfully saved")
              .log();
      return execute;

    } catch (MethodeNotAllowedException e) {
      jsonLogger.field("uuid", uuidString).field("@time", ISO_INSTANT.format(Instant.now()))
              .field("event", "SaveDocStore").field("monitoring_event", "true").field("collection", collection)
              .field("service_name", "document-store-api").message("Error :" +e.getMessage()).log();
      throw ClientError.status(405).exception();

    } catch (ValidationException ex) {
      jsonLogger.field("uuid", uuidString).field("@time", ISO_INSTANT.format(Instant.now()))
              .field("event", "SaveDocStore").field("monitoring_event", "true").field("collection", collection)
              .field("service_name", "document-store-api").message("Error :" +ex.getMessage()).log();
      throw ClientError.status(400).exception();
    }
  }


  @DELETE
  @Timed
  @Path("/{collection}/{uuidString}")
  public Object deleteFromCollection(@PathParam("uuidString") String uuidString,
                                     @javax.ws.rs.core.Context UriInfo uriInfo, @PathParam("collection") String collection) {
    Context context = new Context();
    context.setUuids(uuidString);
    context.setCollection(collection);
    context.setUriInfo(uriInfo);
    try {

      HandlerChain handlerChain = getHandlerChain(collection, Operation.REMOVE);
      Object execute = handlerChain.execute(context);
      jsonLogger.field("uuid", uuidString)
              .field("@time", ISO_INSTANT.format(Instant.now()))
              .field("event", "SaveDocStore")
              .field("collection", collection)
              .field("monitoring_event", "true")
              .field("service_name", "document-store-api")
              .message("Successfully deleted")
              .log();
      return execute;

    } catch (MethodeNotAllowedException e) {
      jsonLogger.field("uuid", uuidString).field("@time", ISO_INSTANT.format(Instant.now()))
              .field("event", "SaveDocStore").field("monitoring_event", "true").field("collection", collection)
              .field("service_name", "document-store-api").message("Error :" +e.getMessage()).log();
      throw ClientError.status(405).exception();

    } catch (ValidationException ex) {
      jsonLogger.field("uuid", uuidString).field("@time", ISO_INSTANT.format(Instant.now()))
              .field("event", "SaveDocStore").field("monitoring_event", "true").field("collection", collection)
              .field("service_name", "document-store-api").message("Error :" +ex.getMessage()).log();
      throw ClientError.status(400).exception();
    }
  }

  protected HandlerChain getHandlerChain(String collection, Operation Operation) {
    Pair<String, Operation> pair = new Pair<>(collection, Operation);
    if (collections.containsKey(pair)) {
      return collections.get(pair);
    }
    collections.keySet().forEach(keyPair -> {
      if (keyPair.getKey().equals(collection)){
        // This method is not allowed
        throw ClientError.status(405).exception();
      }
    });
    throw ClientError.status(400).exception();
  }
}
