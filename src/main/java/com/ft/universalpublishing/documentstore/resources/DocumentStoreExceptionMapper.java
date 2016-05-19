package com.ft.universalpublishing.documentstore.resources;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ft.universalpublishing.documentstore.exception.ValidationException;


public class DocumentStoreExceptionMapper
        extends com.ft.api.jaxrs.errors.RuntimeExceptionMapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentStoreExceptionMapper.class);
    
    @Override
    public Response toResponse(RuntimeException exception) {
        if ((exception instanceof ValidationException) || (exception instanceof IllegalArgumentException)) {
            return respondWith(SC_BAD_REQUEST, exception.getMessage(), exception);
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
        String logMessage = format("Transformer error. Responding with status <%s> and reason <%s>.", status, reason);
        if (400 <= status && status < 500) {
            LOGGER.warn(logMessage, t);
        } else {
            LOGGER.error(logMessage, t);
        }
    }
}

