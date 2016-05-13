package com.ft.universalpublishing.documentstore.resources;

import static com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService.CONTENT_COLLECTION;
import static com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService.LISTS_COLLECTION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.LogLevel;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentMapper;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.transformer.Content;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.transform.ContentBodyProcessingService;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;

@Path("/")
public class DocumentResource {

    protected static final String CHARSET_UTF_8 = ";charset=utf-8";
    
    private static final String LIST_QUERY_PARAM_TEMPLATE = "curated([a-zA-Z]*)For";
    private static final Pattern LIST_QUERY_PARAM_PATTERN = Pattern.compile(LIST_QUERY_PARAM_TEMPLATE);
    
	
	private ContentListValidator contentListValidator;
    private MongoDocumentStoreService documentStoreService;
    private UuidValidator uuidValidator;
    private String apiPath;
    private final ContentMapper contentMapper;
    private final ContentBodyProcessingService bodyProcessingService;

    public DocumentResource(MongoDocumentStoreService documentStoreService,
                            ContentListValidator contentListValidator,
                            UuidValidator uuidValidator,
                            String apiPath,
                            final ContentMapper contentMapper,
                            final ContentBodyProcessingService bodyProcessingService) {
        this.documentStoreService = documentStoreService;
        this.uuidValidator = uuidValidator;
    	this.contentListValidator = contentListValidator;
        this.apiPath = apiPath;
        this.contentMapper = contentMapper;
        this.bodyProcessingService = bodyProcessingService;
    }

	@GET
    @Timed
    @Path("/content/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final Map<String, Object> getContentByUuid(@PathParam("uuidString") String uuidString) {
		validateUuid(uuidString);
	    return findResourceByUuid(CONTENT_COLLECTION, uuidString);
    }

    @GET
    @Timed
    @Path("/content")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final List<Map<String, Object>> getContentByUuids(@QueryParam("uuid") List<String> uuids) {
      Set<UUID> uuidValues = new LinkedHashSet<>();
      for (String uuid : uuids) {
        try {
          validateUuid(uuid);
          uuidValues.add(UUID.fromString(uuid));
        } catch (ValidationException e) {
          /* ignore */
        }
      }
      
      return new ArrayList<>(documentStoreService.findByUuids(CONTENT_COLLECTION, uuidValues));
    }

    @GET
    @Timed
    @Path("/content/{uuidString}/murmur3")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final String getContentHashByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        validateUuid(uuidString);
        Map<String,Object> content = findResourceByUuid(CONTENT_COLLECTION, uuidString);
        
        HashFunction f = Hashing.murmur3_128();
        HashCode h = null;
        for (int i = 0; i < 10000; i++) {
          h = f.hashObject(content, (src,sink) -> consumeObject(src, sink));
        }
        
        return Long.toHexString(h.asLong());
    }

    @GET
    @Timed
    @Path("/content/{uuidString}/murmur3-sorted")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final String getContentSortedHashByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        validateUuid(uuidString);
        Map<String,Object> content = findResourceByUuid(CONTENT_COLLECTION, uuidString);
        
        HashFunction f = Hashing.murmur3_128();
        HashCode h = null;
        for (int i = 0; i < 10000; i++) {
          h = f.hashObject(content, (src,sink) -> consumeSortedObject(src, sink));
        }
        
        return Long.toHexString(h.asLong());
    }
    
    private void consumeObject(Map<String,Object> src, PrimitiveSink sink) {
      sink.putChar('{');
      
      boolean empty = true;
      boolean sorted = (src instanceof SortedMap);
      for (Map.Entry<String,Object> en : src.entrySet()) {
        if (!empty) {
          sink.putChar(',');
          empty = false;
        }
        
        sink.putString(en.getKey(), UTF_8);
        sink.putChar(':');
        
        consumeValue(en.getValue(), sink, sorted);
      }
      sink.putChar('}');
    }
    
    private void consumeSortedObject(Map<String,Object> src, PrimitiveSink sink) {
      consumeObject(new TreeMap<>(src), sink);
    }
    
    private void consumeCollection(Collection values, PrimitiveSink sink, boolean sorted) {
      boolean empty = true;
      sink.putChar('[');
      
      for (Object value : values) {
        if (!empty) {
          sink.putChar(',');
          empty = false;
        }
        
        consumeValue(value, sink, sorted);
      }
      
      sink.putChar(']');
    }
    
    private void consumeValue(Object value, PrimitiveSink sink, boolean sorted) {
      if (value == null) {
        sink.putString("null", UTF_8);
      } else {
        Class<?> cl = value.getClass();
        
        if (cl == String.class) {
          sink.putChar('"');
          sink.putString((String)value, UTF_8);
          sink.putChar('"');
        } else if (Number.class.isAssignableFrom(cl)) {
          sink.putLong(((Number)value).longValue());
        } else if (cl == Boolean.class) {
          sink.putString(Boolean.toString((boolean)value), UTF_8);
        } else if (Collection.class.isAssignableFrom(cl)) {
          consumeCollection((Collection)value, sink, sorted);
        } else if (Map.class.isAssignableFrom(cl)) {
          sink.putChar('{');
          if (sorted) {
            consumeSortedObject((Map)value, sink);
          } else {
            consumeObject((Map)value, sink);
          }
          sink.putChar('}');
        }
      }
      
    }

    @GET
    @Timed
    @Path("/content-read/{uuid}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final com.ft.universalpublishing.documentstore.model.read.Content getContentReadByUuid(@PathParam("uuid") String uuid,
            @Context ApiUriGenerator currentUriGenerator) {
        validateUuid(uuid);
        final Map<String, Object> resource = findResourceByUuid(CONTENT_COLLECTION, uuid);
        final Content content = new ObjectMapper().convertValue(resource, Content.class);
        return bodyProcessingService.process(contentMapper.map(content), currentUriGenerator);
    }
    
    @GET
    @Timed
    @Path("/lists/{uuidString}")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final ContentList getListsByUuid(@PathParam("uuidString") String uuidString, @Context HttpHeaders httpHeaders) {
        validateUuid(uuidString);
        Map<String, Object> contentMap = findResourceByUuid(LISTS_COLLECTION, uuidString);
        try {
            return convertToContentList(contentMap);
        } catch (IllegalArgumentException e) {
            throw ClientError.status(SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
    }
    
    @GET
    @Timed
    @Path("/lists")
    @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
    public final ContentList getListsByConceptAndType(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        
        if (queryParameters.size() ==0) {
            throw ClientError.status(SC_BAD_REQUEST).error("Expected at least one query parameter").exception();
        }
        Set<String> keys = queryParameters.keySet();
        
        String listType = null;
        String conceptId = null;
        
        for (String key: keys) {
            Matcher matcher = LIST_QUERY_PARAM_PATTERN.matcher(key);
            boolean found = matcher.find();
            if (found) {
                listType = matcher.group(1);
                conceptId = queryParameters.getFirst(key);
            }
        }
        
        if (listType == null) {
            throw ClientError.status(SC_BAD_REQUEST).error("Expected at least one query parameter of the form \"curated<listType>For\"").exception();
        }

        Map<String,Object> result = documentStoreService.findByConceptAndType(LISTS_COLLECTION, conceptId, listType);
        if (result == null) {
            throw ClientError.status(SC_NOT_FOUND).logLevel(LogLevel.DEBUG).error("Requested item does not exist").exception();
        }
        return convertToContentList(result);
    }

    protected ContentList convertToContentList(Map<String, Object> contentMap) {
        ContentList contentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
        contentList.addIds();
        contentList.addApiUrls(apiPath);
        contentList.removePrivateFields();
        return contentList;
    }

    @PUT
    @Timed
    @Path("/content/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeContent(@PathParam("uuidString") String uuidString, Map<String, Object> contentMap, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
    	return writeDocument(CONTENT_COLLECTION, contentMap, uriInfo);
    
    }


    @PUT
    @Timed
    @Path("/lists/{uuidString}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response writeLists(@PathParam("uuidString") String uuidString, Map<String, Object> contentMap, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        
        ContentList contentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
        contentListValidator.validate(uuidString, contentList);
        
        return writeDocument(LISTS_COLLECTION, contentMap, uriInfo);
    
    }

    private Response writeDocument(String resourceType, Map<String, Object> content, UriInfo uriInfo) {
      final DocumentWritten written = documentStoreService.write(resourceType, content);
      final Response response;
      switch (written.getMode()) {
        case Created:
          response = Response.created(uriInfo.getRequestUri()).build();
          break;
        case Updated:
          response = Response.ok(written.getDocument()).build();
          break;
        default:
          throw new IllegalStateException("unknown write mode " + written.getMode());
      }
      return response;
    }
    
    @DELETE
    @Timed
    @Path("/content/{uuidString}")
    public Response deleteContent(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        return delete(CONTENT_COLLECTION, uuidString);
    }
    
    @DELETE
    @Timed
    @Path("/lists/{uuidString}")
    public Response deleteList(@PathParam("uuidString") String uuidString, @Context UriInfo uriInfo) {
        validateUuid(uuidString);
        return delete(LISTS_COLLECTION, uuidString);
    }

    private Response delete(String resourceType, String uuidString) {
        try {
            documentStoreService.delete(resourceType, UUID.fromString(uuidString));
            return Response.ok().build();
        } catch (DocumentNotFoundException e){
            return Response.ok().build();
        }
    }

    protected void validateUuid(String uuidString) {
      uuidValidator.validate(uuidString);
    }

    protected Map<String, Object> findResourceByUuid(final String resourceType, final String uuid) {
      final Map<String, Object> foundDocument = documentStoreService.findByUuid(resourceType, UUID.fromString(uuid));
      return foundDocument;
    }
}
