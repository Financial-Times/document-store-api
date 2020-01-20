package com.ft.universalpublishing.documentstore.resources;

import static com.ft.api.jaxrs.errors.ClientError.status;
import static com.ft.universalpublishing.documentstore.model.read.Operation.ADD;
import static com.ft.universalpublishing.documentstore.model.read.Operation.GET_BY_ID;
import static com.ft.universalpublishing.documentstore.model.read.Operation.GET_FILTERED;
import static com.ft.universalpublishing.documentstore.model.read.Operation.GET_MULTIPLE_FILTERED;
import static com.ft.universalpublishing.documentstore.model.read.Operation.REMOVE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.MESSAGE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_DELETE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_PUT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.UUID;

import java.util.List;
import java.util.Map;

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
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

@Path("/")
public class DocumentResource {

    protected static final String CHARSET_UTF_8 = ";charset=utf-8";

    private Map<Pair<String, Operation>, HandlerChain> collections;
    
    private FluentLoggingWrapper logger;
    
    public DocumentResource(Map<Pair<String, Operation>, HandlerChain> collections) {
        this.collections = collections;
        logger = new FluentLoggingWrapper();
        logger.withClassName(this.getClass().getCanonicalName());
    }

    @GET
    @Timed
    @Path("/{collection}/{uuidString}")
    @Produces(APPLICATION_JSON + CHARSET_UTF_8)
    public final Object getFromCollectionByUuid(
            @PathParam("uuidString") String uuidString, @PathParam("collection") String collection) {
        Context context = new Context();
        context.setUuids(uuidString);
        context.setCollection(collection);
        
        HandlerChain handlerChain = getHandlerChain(collection, GET_BY_ID);
        return handlerChain.execute(context);
    }

    @GET
    @Timed
    @Path("/{collection}")
    @Produces(APPLICATION_JSON + CHARSET_UTF_8)
    public final Object getFromCollectionByUuids(
            @javax.ws.rs.core.Context HttpHeaders httpHeaders,
            @javax.ws.rs.core.Context UriInfo uriInfo, @PathParam("collection") String collection) {
        Context context = new Context();
        context.setUriInfo(uriInfo);
        context.setHttpHeaders(httpHeaders);
        context.setCollection(collection);
        
        HandlerChain handlerChain = getHandlerChain(collection, GET_FILTERED);
        return handlerChain.execute(context);
    }

    @POST
    @Timed
    @Path("/{collection}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON + CHARSET_UTF_8)
    public final Object getFromCollectionByUuids(@javax.ws.rs.core.Context HttpHeaders httpHeaders,
                                                 List<String> uuidList,
                                                 @javax.ws.rs.core.Context UriInfo uriInfo,
                                                 @PathParam("collection") String collection,
                                                 @QueryParam("mget") boolean mget) {
        if (!mget) {
            throw status(400).exception(new IllegalArgumentException("This is an endpoint for retrieving data, not writing. You must supply query parameter ?mget=true"));
        }
        if (uuidList == null) {
            throw status(400).exception(new IllegalArgumentException("Incorrect format of request body. It's not an array of strings."));
        }

        Context context = new Context();
        context.setUriInfo(uriInfo);
        context.setHttpHeaders(httpHeaders);
        context.setCollection(collection);
        context.setUuids(uuidList);

        HandlerChain handlerChain = getHandlerChain(collection, GET_MULTIPLE_FILTERED);
        return handlerChain.execute(context);
    }

    @PUT
    @Timed
    @Path("/{collection}/{uuidString}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Object writeInCollection(@PathParam("uuidString") String uuidString,
                                    Map<String, Object> contentMap, @javax.ws.rs.core.Context UriInfo uriInfo,
                                    @PathParam("collection") String collection) {

        Context context = new Context();
        context.setCollection(collection);
        context.setUuids(uuidString);
        context.setContentMap(contentMap);
        context.setUriInfo(uriInfo);

        logger.withMetodName("writeInCollection")
                .withRequest(context, METHOD_PUT, "/{collection}/{uuidString}")
                .withUriInfo(uriInfo).withField(UUID, uuidString);

        try {
            HandlerChain handlerChain = getHandlerChain(collection, ADD);
            Object result = handlerChain.execute(context);
            logger.withField(MESSAGE, "Successfully saved").build().log();

            return result;

        } catch (WebApplicationClientException ex) {
            logger.withField(MESSAGE, "Error: " + ex.getMessage()).build().logError();

            throw ex;
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

        logger.withMetodName("deleteFromCollection")
                .withRequest(context, METHOD_DELETE, "/{collection}/{uuidString}")
                .withUriInfo(uriInfo).withField(UUID, uuidString);

        try {
            HandlerChain handlerChain = getHandlerChain(collection, REMOVE);
            Object result = handlerChain.execute(context);
            logger.withField(MESSAGE, "Successfully deleted").build().log();

            return result;

        } catch (WebApplicationClientException e) {
            logger.withField(MESSAGE, "Error: " + e.getMessage()).build().logError();

            throw e;
        }
    }

    protected HandlerChain getHandlerChain(String collection, Operation Operation) {
        Pair<String, Operation> pair = new Pair<>(collection, Operation);
        if (collections.containsKey(pair)) {
            return collections.get(pair);
        }
        collections.keySet().forEach(keyPair -> {
            if (keyPair.getKey().equals(collection)) {
                // This method is not allowed
                throw ClientError.status(405).exception();
            }
        });
        throw ClientError.status(400).exception();
    }
}
