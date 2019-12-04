package com.ft.universalpublishing.documentstore.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.MDC.get;

public class IDStreamingException extends WebApplicationException {
    
    private Response response;
    
    public IDStreamingException(String collection) {
        super(status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(APPLICATION_JSON)
                .entity(singletonMap("message","An error occurred when trying to get ids for collection " + collection))
                .build());
        response = constructResponse(collection);
        
        new FluentLoggingWrapper().withClassName(this.getClass().getCanonicalName())
                .withMetodName("IDStreamingException").withResponse(response).withTransactionId(get(TRANSACTION_ID))
                .withField(METHOD, "GET").withField(FluentLoggingUtils.MESSAGE,
                        "An error occurred when trying to get ids for collection " + collection)
                .build().logError();
    }
    
    private Response constructResponse(String collection) {
        response = status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(APPLICATION_JSON)
                .entity(singletonMap("message","An error occurred when trying to get ids for collection " + collection))
                .build();
        return response;
    }
}
