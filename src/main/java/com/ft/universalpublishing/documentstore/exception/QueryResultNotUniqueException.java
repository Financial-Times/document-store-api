package com.ft.universalpublishing.documentstore.exception;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.slf4j.MDC.get;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

public class QueryResultNotUniqueException extends WebApplicationException {
    private Response response;

    public QueryResultNotUniqueException() {
        super(status(INTERNAL_SERVER_ERROR)
                .type(APPLICATION_JSON)
                .entity(singletonMap("message", format("There is a duplicate record for this identifier! Please contact an administrator.")))
                .build());
        response = constructResponse();

        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "QueryResultNotUniqueException").withResponse(response)
                .withTransactionId(get(TRANSACTION_ID)).withField(METHOD, "GET")
                .withField(FluentLoggingUtils.MESSAGE,
                        format("There is a duplicate record for this identifier! Please contact an administrator."))
                .build().logError();
    }

    private Response constructResponse() {
        response = status(Response.Status.INTERNAL_SERVER_ERROR).type(APPLICATION_JSON)
                .entity(singletonMap("message",
                        format("There is a duplicate record for this identifier! Please contact an administrator.")))
                .build();
        return response;
    }

}
