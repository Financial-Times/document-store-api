package com.ft.universalpublishing.documentstore.exception;

import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.UUID;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.status;
import static org.slf4j.MDC.get;

public class DocumentNotFoundException extends WebApplicationException {

    private static final ErrorMessage MESSAGE = new ErrorMessage("Requested item does not exist");

    private final UUID uuid;
    private FluentLoggingWrapper logger;

    public DocumentNotFoundException(UUID uuid) {
        super();
        this.uuid = uuid;
        logger = new FluentLoggingWrapper();
        logger.withClassName(this.getClass().getCanonicalName());
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getMessage() {
        return format("Document with uuid : %s not found!", uuid);
    }

    @Override
    public Response getResponse() {
        Response response = status(SC_NOT_FOUND).type(APPLICATION_JSON).entity(MESSAGE).build();

        logger.withMetodName("getResponse").withResponse(response).withTransactionId(get(TRANSACTION_ID))
                .withField(METHOD, "GET").withField(FluentLoggingUtils.MESSAGE, getMessage()).withField(UUID, uuid)
                .build().logWarn();

        return response;
    }
}
