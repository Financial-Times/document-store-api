package com.ft.universalpublishing.documentstore.handler;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.model.read.Context;

public class ExtractConceptHandler implements Handler {

    private static final String LIST_QUERY_PARAM_TEMPLATE = "curated([a-zA-Z]*)For";
    private static final Pattern LIST_QUERY_PARAM_PATTERN = Pattern.compile(LIST_QUERY_PARAM_TEMPLATE);

    @Override
    public void handle(Context context) {
        MultivaluedMap<String, String> queryParameters = context.getUriInfo().getQueryParameters();
        if (queryParameters.isEmpty()) {
            throw ClientError.status(SC_BAD_REQUEST).error("Expected at least one query parameter").exception();
        }

        Set<String> keys = queryParameters.keySet();

        String listType = null;
        UUID conceptUUID = null;

        for (String key : keys) {
            Matcher matcher = LIST_QUERY_PARAM_PATTERN.matcher(key);
            boolean found = matcher.find();
            if (found) {
                listType = matcher.group(1);
                try {
                    conceptUUID = UUID.fromString(queryParameters.getFirst(key));
                } catch (IllegalArgumentException e) {
                    throw ClientError.status(SC_BAD_REQUEST).error("The concept ID is not a valid UUID").exception();
                }
            }
        }

        if (listType == null) {
            throw ClientError.status(SC_BAD_REQUEST)
                    .error("Expected at least one query parameter of the form \"curated<listType>For\"").exception();
        }

        context.setConceptUUID(conceptUUID.toString());
        context.addParameter("listType", listType);
    }
}
