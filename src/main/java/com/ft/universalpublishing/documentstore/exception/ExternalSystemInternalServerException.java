package com.ft.universalpublishing.documentstore.exception;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExternalSystemInternalServerException extends WebApplicationException {

  private static final ErrorMessage MESSAGE =
      new ErrorMessage("Internal error communicating with external system");

  public ExternalSystemInternalServerException(Throwable cause) {
    super(cause);
  }

  @Override
  public Response getResponse() {
    return Response.status(SC_INTERNAL_SERVER_ERROR)
        .type(MediaType.APPLICATION_JSON)
        .entity(MESSAGE)
        .build();
  }
}
