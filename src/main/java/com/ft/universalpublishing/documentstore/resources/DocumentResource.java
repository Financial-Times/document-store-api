package com.ft.universalpublishing.documentstore.resources;

import static com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService.CONTENT_COLLECTION;
import static com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService.LISTS_COLLECTION;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
