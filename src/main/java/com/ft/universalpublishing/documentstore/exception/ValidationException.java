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
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class ValidationException extends WebApplicationException {

    public ValidationException(String message) {
        super(message);
    }

    @Override
    public Response getResponse() {
        Response response = status(SC_BAD_REQUEST).type(APPLICATION_JSON).entity(new ErrorMessage(getMessage()))
                .build();
        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "getResponse").withResponse(response)
                .withTransactionId(get(TRANSACTION_ID)).withField(METHOD, "GET")
                .withField(FluentLoggingUtils.MESSAGE, getMessage()).build().logWarn();

        return response;
    }
}
