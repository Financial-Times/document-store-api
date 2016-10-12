package com.ft.universalpublishing.documentstore.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import javax.ws.rs.core.MediaType;

public class ExternalSystemUnavailableException extends WebApplicationException {

  private static final ErrorMessage MESSAGE = new ErrorMessage("Service Unavailable");

  public ExternalSystemUnavailableException(String message) {
    super(message);
  }

  public ExternalSystemUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public Response getResponse() {
    return Response.status(SC_SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON).entity(MESSAGE).build();
  }
}
