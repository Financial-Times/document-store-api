package com.ft.universalpublishing.documentstore.exception;

import java.util.Collections;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class QueryResultNotUniqueException extends WebApplicationException {

    public QueryResultNotUniqueException() {
        super(Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(Collections.singletonMap("message", String.format("There is a duplicate record for this identifier! Please contact an administrator.")))
                .build());
    }
}
