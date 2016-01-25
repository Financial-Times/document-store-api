package com.ft.universalpublishing.documentstore.resources;

import static com.ft.universalpublishing.documentstore.resources.DocumentResource.CHARSET_UTF_8;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.util.ApiUriGenerator;
import com.google.common.base.Strings;


@Path("/content-query")
@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
public class DocumentQueryResource {
    private final DocumentStoreService documentStoreService;
    
    public DocumentQueryResource(DocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @GET
    @Timed
    public final Response findContentByIdentifier(@QueryParam("identifierAuthority") String authority, @QueryParam("identifierValue") String identifierValue,
            @Context ApiUriGenerator currentUriGenerator) {
        
        Map<String,Object> query = new LinkedHashMap<>();
        query.put("identifiers.identifierValue", identifierValue);
        if (!Strings.isNullOrEmpty(authority)) {
            query.put("identifiers.authority", authority);
        }
        
        Map<String,Object> content = documentStoreService.findSingleItemByQueryFields("content", query);
        if (content == null) {
            return Response.status(404).entity(Collections.singletonMap("message", String.format("Not found: %s.", query))).build();
        }
        
        URI location = URI.create(currentUriGenerator.resolve("/content/" + content.get("uuid")));
        return Response.status(301).location(location).build();
    }
}
