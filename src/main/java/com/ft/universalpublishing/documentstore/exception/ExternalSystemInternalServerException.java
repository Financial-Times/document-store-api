package com.ft.universalpublishing.documentstore.exception;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.MDC.get;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class ExternalSystemInternalServerException extends WebApplicationException {

    private static final ErrorMessage MESSAGE = new ErrorMessage("Internal error communicating with external system");

    public ExternalSystemInternalServerException(Throwable cause) {
        super(cause);
    }

    @Override
    public Response getResponse() {
        Response response = status(SC_INTERNAL_SERVER_ERROR).type(APPLICATION_JSON).entity(MESSAGE).build();

        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "getResponse").withResponse(response)
                .withTransactionId(get(TRANSACTION_ID)).withField(METHOD, "GET")
                .withField(FluentLoggingUtils.MESSAGE, MESSAGE.getMessage()).build().logError();

        return response;
    }
}
