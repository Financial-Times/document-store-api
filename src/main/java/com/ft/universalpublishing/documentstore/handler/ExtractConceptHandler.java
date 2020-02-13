package com.ft.universalpublishing.documentstore.handler;

import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import static com.ft.api.jaxrs.errors.ClientError.status;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.MESSAGE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.STATUS;
import static java.util.UUID.fromString;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class ExtractConceptHandler implements Handler {

    private static final String LIST_QUERY_PARAM_TEMPLATE = "curated([a-zA-Z]*)For";
    private static final Pattern LIST_QUERY_PARAM_PATTERN = Pattern.compile(LIST_QUERY_PARAM_TEMPLATE);

    @Override
    public void handle(Context context) {
        MultivaluedMap<String, String> queryParameters = context.getUriInfo().getQueryParameters();
        WebApplicationClientException clientException;
        FluentLoggingBuilder loggerBuilder = FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(),
                "handle");
        if (queryParameters.isEmpty()) {
            clientException = status(SC_BAD_REQUEST).error("Expected at least one query parameter").exception();
            loggerBuilder.withField(STATUS, SC_BAD_REQUEST).withField(MESSAGE, "Expected at least one query parameter")
                    .withException(clientException).build().logWarn();
            throw clientException;
        }

        Set<String> keys = queryParameters.keySet();

        String listType = null;
        UUID conceptId = null;

        for (String key : keys) {
            Matcher matcher = LIST_QUERY_PARAM_PATTERN.matcher(key);
            boolean found = matcher.find();
            if (found) {
                listType = matcher.group(1);
                try {
                    conceptId = fromString(queryParameters.getFirst(key));
                } catch (IllegalArgumentException e) {
                    clientException = status(SC_BAD_REQUEST).error("The concept ID is not a valid UUID").exception();
                    loggerBuilder.withField(STATUS, SC_BAD_REQUEST)
                            .withField(MESSAGE, "The concept ID is not a valid UUID").withException(clientException)
                            .build().logWarn();

                    throw clientException;
                }
            }
        }

        if (listType == null) {
            clientException = status(SC_BAD_REQUEST)
                    .error("Expected at least one query parameter of the form \"curated<listType>For\"").exception();
            loggerBuilder.withField(STATUS, SC_BAD_REQUEST)
                    .withField(MESSAGE,
                            "Expected at least one query parameter of the form \\\"curated<listType>For\\\"")
                    .withException(clientException).build().logWarn();

            throw clientException;
        }

        context.addParameter("conceptId", conceptId);
        context.addParameter("listType", listType);
    }
}
