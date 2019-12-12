package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;
import com.codahale.metrics.annotation.Timed;
import com.google.common.net.HostAndPort;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.ft.universalpublishing.documentstore.resources.DocumentResource.CHARSET_UTF_8;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.MESSAGE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_GET;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.UriBuilder.fromPath;
import static org.slf4j.MDC.get;

@Path("/content-query")
@Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
public class DocumentQueryResource {

    private final MongoDocumentStoreService documentStoreService;
    private final HostAndPort apiHost;
    private FluentLoggingWrapper logger;

    public DocumentQueryResource(MongoDocumentStoreService documentStoreService, String apiHost) {
        this.documentStoreService = documentStoreService;
        this.apiHost = HostAndPort.fromString(apiHost);
        logger = new FluentLoggingWrapper();
        logger.withClassName(this.getClass().getCanonicalName());
    }

    @GET
    @Timed
    public final Response findContentByIdentifier(@QueryParam("identifierAuthority") String authority,
            @QueryParam("identifierValue") String identifierValue) {
        Response response;
        logger.withMetodName("findContentByIdentifier").withTransactionId(get(TRANSACTION_ID)).withField(METHOD, METHOD_GET);

        if (isNullOrEmpty(authority) || isNullOrEmpty(identifierValue)) {
            response = status(SC_BAD_REQUEST).entity(singletonMap("message",
                    "Query parameters \"identifierAuthority\" and \"identifierValue\" are required.")).build();
            logger.withResponse(response)
                    .withField(MESSAGE,
                            "Query parameters \"identifierAuthority\" and \"identifierValue\" are required.")
                    .build().logWarn();

            return response;
        }

        Map<String, Object> content = documentStoreService.findByIdentifier("content", authority, identifierValue);
        if (content == null) {
            response = status(SC_NOT_FOUND)
                    .entity(singletonMap("message", format("Not found: %s:%s", authority, identifierValue))).build();
            logger.withResponse(response).withField(MESSAGE, format("Not found: %s:%s", authority, identifierValue))
                    .build().logWarn();

            return response;
        }

        String uuid = content.get("uuid").toString();
        response = status(SC_MOVED_PERMANENTLY).location(createApiUri(uuid)).build();
        logger.withUriInfo(createApiUri(uuid)).withResponse(response)
                .withField(MESSAGE, format("Not found: %s:%s", authority, identifierValue)).build().logError();

        return response;
    }

    private URI createApiUri(String uuid) {
        return fromPath("/content/" + uuid).scheme("http").host(apiHost.getHostText())
                .port(apiHost.getPortOrDefault(-1)).build();
    }
}
