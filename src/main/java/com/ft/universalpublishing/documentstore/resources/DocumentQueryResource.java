package com.ft.universalpublishing.documentstore.resources;

import static com.ft.universalpublishing.documentstore.resources.DocumentResource.CHARSET_UTF_8;
import static javax.servlet.http.HttpServletResponse.*;

import com.codahale.metrics.annotation.Timed;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import io.swagger.annotations.Api;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Api(tags = {"collections"})
@Path("/content-query")
@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
public class DocumentQueryResource {

  private final MongoDocumentStoreService documentStoreService;
  private final HostAndPort apiHost;

  public DocumentQueryResource(MongoDocumentStoreService documentStoreService, String apiHost) {
    this.documentStoreService = documentStoreService;
    this.apiHost = HostAndPort.fromString(apiHost);
  }

  @GET
  @Timed
  public final Response findContentByIdentifier(
      @QueryParam("identifierAuthority") String authority,
      @QueryParam("identifierValue") String identifierValue) {
    if (Strings.isNullOrEmpty(authority) || Strings.isNullOrEmpty(identifierValue)) {
      return Response.status(SC_BAD_REQUEST)
          .entity(
              Collections.singletonMap(
                  "message",
                  "Query parameters \"identifierAuthority\" and \"identifierValue\" are required."))
          .build();
    }

    Map<String, Object> content =
        documentStoreService.findByIdentifier("content", authority, identifierValue);
    if (content == null) {
      return Response.status(SC_NOT_FOUND)
          .entity(
              Collections.singletonMap(
                  "message", String.format("Not found: %s:%s", authority, identifierValue)))
          .build();
    }

    return Response.status(SC_MOVED_PERMANENTLY)
        .location(createApiUri(content.get("uuid").toString()))
        .build();
  }

  private URI createApiUri(String uuid) {
    return UriBuilder.fromPath("/content/" + uuid)
        .scheme("http")
        .host(apiHost.getHost())
        .port(apiHost.getPortOrDefault(-1))
        .build();
  }
}
