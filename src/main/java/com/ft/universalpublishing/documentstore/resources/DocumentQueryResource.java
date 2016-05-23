package com.ft.universalpublishing.documentstore.resources;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import static com.ft.universalpublishing.documentstore.resources.DocumentResource.CHARSET_UTF_8;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
import com.google.common.base.Strings;


@Path("/content-query")
@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
public class DocumentQueryResource {
    private final MongoDocumentStoreService documentStoreService;
    
    public DocumentQueryResource(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @GET
    @Timed
    public final Response findContentByIdentifier(@QueryParam("identifierAuthority") String authority, @QueryParam("identifierValue") String identifierValue,
            @Context ApiUriGenerator currentUriGenerator) {
        
      if (Strings.isNullOrEmpty(authority) || Strings.isNullOrEmpty(identifierValue)) {
        return Response.status(SC_BAD_REQUEST).entity(
          Collections.singletonMap("message",
            "Query parameters \"identifierAuthority\" and \"identifierValue\" are required."))
          .build();
      }
      
        Map<String,Object> content = documentStoreService.findByIdentifier("content", authority, identifierValue);
        if (content == null) {
            return Response.status(SC_NOT_FOUND).entity(Collections.singletonMap("message", String.format("Not found: %s:%s", authority, identifierValue))).build();
        }
        
        URI location = URI.create(currentUriGenerator.resolve("/content/" + content.get("uuid")));
        return Response.status(SC_MOVED_PERMANENTLY).location(location).build();
    }
}
