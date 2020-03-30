package com.ft.universalpublishing.documentstore.exception;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.UUID;

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
		StringBuilder message = new StringBuilder();
		for (int i = 0; i < uuids.length; i++) {
			message.append(uuids[i]).append(" ");
		}

		return String.format("Documents not found for uuids: %s", message.toString());
	}

	@Override
	public Response getResponse() {
		return Response.status(SC_NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(MESSAGE).build();
	}
}
