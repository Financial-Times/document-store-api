package com.ft.universalpublishing.documentstore.exception;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DocumentsNotFoundException extends WebApplicationException {

  private static final ErrorMessage MESSAGE = new ErrorMessage("Requested item does not exist");

  private final UUID[] uuids;

  public DocumentsNotFoundException(UUID[] uuids) {
    super();
    this.uuids = uuids;
  }

  public UUID[] getUuids() {
    return uuids;
  }

  @Override
  public String getMessage() {
    String message =
        String.join(
            ", ",
            Arrays.asList(uuids).stream()
                .map(uuid -> uuid.toString())
                .collect(Collectors.toList()));

    return String.format("Documents not found for uuids: %s", message);
  }

  @Override
  public Response getResponse() {
    return Response.status(SC_NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(MESSAGE).build();
  }
}
