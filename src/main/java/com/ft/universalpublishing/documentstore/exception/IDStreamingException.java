package com.ft.universalpublishing.documentstore.exception;

import java.util.Collections;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class IDStreamingException extends WebApplicationException {

  public IDStreamingException(String collection) {
    super(
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(
                Collections.singletonMap(
                    "message",
                    "An error occurred when trying to get ids for collection " + collection))
            .build());
  }
}
