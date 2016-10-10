package com.ft.universalpublishing.documentstore.resources;

import com.codahale.metrics.annotation.Timed;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.google.common.base.Strings;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.ft.universalpublishing.documentstore.resources.DocumentResource.CHARSET_UTF_8;
import static javax.servlet.http.HttpServletResponse.*;

@Path("/content-query")
@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
public class DocumentQueryResource {

    private final MongoDocumentStoreService documentStoreService;
    private final String apiHost;

    public DocumentQueryResource(MongoDocumentStoreService documentStoreService, String apiHost) {
        this.documentStoreService = documentStoreService;
        this.apiHost = apiHost;
    }

    @GET
    @Timed
    public final Response findContentByIdentifier(@QueryParam("identifierAuthority") String authority, @QueryParam("identifierValue") String identifierValue) {
        if (Strings.isNullOrEmpty(authority) || Strings.isNullOrEmpty(identifierValue)) {
            return Response.status(SC_BAD_REQUEST).entity(
                    Collections.singletonMap("message",
                            "Query parameters \"identifierAuthority\" and \"identifierValue\" are required."))
                    .build();
        }

        Map<String, Object> content = documentStoreService.findByIdentifier("content", authority, identifierValue);
        if (content == null) {
            return Response.status(SC_NOT_FOUND).entity(Collections.singletonMap("message", String.format("Not found: %s:%s", authority, identifierValue))).build();
        }

        URI location = URI.create(apiHost + "/content/" + content.get("uuid"));
        return Response.status(SC_MOVED_PERMANENTLY).location(location).build();
    }
}
