package com.ft.universalpublishing.documentstore.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.model.read.DocumentUUID;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;


@Path("/")
public class DocumentResource {

  protected static final String CHARSET_UTF_8 = ";charset=utf-8";

  private Map<Pair<String, Operation>, HandlerChain> collections;
  private ObjectMapper objectMapper;

  public DocumentResource(Map<Pair<String, Operation>, HandlerChain> collections, ObjectMapper objectMapper) {
    this.collections = collections;
    this.objectMapper = objectMapper;
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

  @GET
  @Path("/{collection}/__ids")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Response getIDsForCollection(@PathParam("collection") String collection) {
    Context context = new Context();
    context.setCollection(collection);
    HandlerChain handlerChain = getHandlerChain(collection, Operation.GET_IDS);
    Set result = (Set) handlerChain.execute(context);

    return Response.ok(getStreamingOutput(result)).build();
  }

  private StreamingOutput getStreamingOutput(Set result) {
    return outputStream -> {
      Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
      for (Object document : result) {
        String value = ((Document) document).getString("uuid");
        writer.write(objectMapper.writeValueAsString(new DocumentUUID(UUID.fromString(value))));
      }
      writer.flush();
    };
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
    HandlerChain handlerChain = getHandlerChain(collection, Operation.ADD);
    return handlerChain.execute(context);
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
    HandlerChain handlerChain = getHandlerChain(collection, Operation.REMOVE);
    return handlerChain.execute(context);
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
