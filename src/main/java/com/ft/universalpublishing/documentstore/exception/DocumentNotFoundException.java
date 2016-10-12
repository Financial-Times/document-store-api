package com.ft.universalpublishing.documentstore.exception;

import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import javax.ws.rs.core.MediaType;

public class DocumentNotFoundException extends WebApplicationException {

  private static final ErrorMessage MESSAGE = new ErrorMessage("Requested item does not exist");

	private final UUID uuid;

	public DocumentNotFoundException(UUID uuid) {
		super();
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String getMessage(){
		return String.format("Document with uuid : %s not found!", uuid);
	}

  @Override
  public Response getResponse() {
    return Response.status(SC_NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(MESSAGE).build();
  }
}
