package com.ft.universalpublishing.documentstore.exception;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.MDC.get;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

public class ExternalSystemUnavailableException extends WebApplicationException {

    private static final ErrorMessage MESSAGE = new ErrorMessage("Service Unavailable");

    private FluentLoggingWrapper logger;

    public ExternalSystemUnavailableException(String message) {
        super(message);
        logger = new FluentLoggingWrapper();
        logger.withClassName(this.getClass().getCanonicalName());
    }

    public ExternalSystemUnavailableException(String message, Throwable cause) {
        super(message, cause);
        logger = new FluentLoggingWrapper();
        logger.withClassName(this.getClass().getCanonicalName());
    }

    @Override
    public Response getResponse() {
        Response response = status(SC_SERVICE_UNAVAILABLE).type(APPLICATION_JSON).entity(MESSAGE).build();

        logger.withMetodName("getResponse").withResponse(response).withTransactionId(get(TRANSACTION_ID))
                .withField(METHOD, "GET").withField(FluentLoggingUtils.MESSAGE, MESSAGE.getMessage()).build()
                .logError();

        return response;
    }
}
