package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemInternalServerException;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;


public class DocumentStoreExceptionMapper
        extends com.ft.api.jaxrs.errors.RuntimeExceptionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentStoreExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        if ((exception instanceof ValidationException) || (exception instanceof IllegalArgumentException)) {
            return respondWith(SC_BAD_REQUEST, exception.getMessage(), exception);
        }

        if (exception instanceof NotAllowedException){
            return respondWith(SC_METHOD_NOT_ALLOWED, "Method not allowed.", exception);
        }

        if (exception instanceof DocumentNotFoundException) {
            return respondWith(SC_NOT_FOUND, "Requested item does not exist", exception);
        }

        if (exception instanceof ExternalSystemUnavailableException) {
            return respondWith(SC_SERVICE_UNAVAILABLE, "Service Unavailable", exception);
        }

        if (exception instanceof ExternalSystemInternalServerException) {
            return respondWith(SC_INTERNAL_SERVER_ERROR, "Internal error communicating with external system", exception);
        }

        return super.toResponse(exception);
    }

    private Response respondWith(int status, String reason, Throwable t) {
        return respondWith(status, reason, t, Collections.emptyMap());
    }

    private Response respondWith(int status, String reason, Throwable t, Map<String,Object> context) {
        logResponse(status, reason, t);

        Map<String,Object> responseMessage = new HashMap<>(context);
        responseMessage.put("message", reason);
        return Response.serverError().status(status).entity(responseMessage).type(APPLICATION_JSON_TYPE).build();
    }

    private void logResponse(int status, String reason, Throwable t) {
        String logMessage = format("Document store error. Responding with status <%s> and reason <%s>.", status, reason);
        if (status == 404){
            LOGGER.debug(logMessage, t); // Debug level can be switched on via the Dropwizard Admin page (within the docker container).
        } else if (400 <= status && status < 500) {
            LOGGER.warn(logMessage, t);
        } else {
            LOGGER.error(logMessage, t);
        }
    }
}

