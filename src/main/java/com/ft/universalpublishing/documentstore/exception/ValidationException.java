package com.ft.universalpublishing.documentstore.exception;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ValidationException extends WebApplicationException {

  public ValidationException(String message) {
    super(message);
  }

  @Override
  public Response getResponse() {
    return Response.status(SC_BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(new ErrorMessage(getMessage()))
        .build();
  }
}
