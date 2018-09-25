package com.ft.universalpublishing.documentstore.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

public class MethodeNotAllowedException extends WebApplicationException {
    public MethodeNotAllowedException(String message) {
        super(message);
    }

    @Override
    public Response getResponse() {
        return Response.status(SC_METHOD_NOT_ALLOWED).type(MediaType.APPLICATION_JSON).entity(new ErrorMessage(getMessage())).build();
    }
}
