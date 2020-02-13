package com.ft.universalpublishing.documentstore.handler;


import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.ACCEPT;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.HOST;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.MESSAGE;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_GET;
import static java.util.Collections.emptySet;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class MultipleUuidValidationHandler implements Handler {

    private UuidValidator validator;

    public MultipleUuidValidationHandler(UuidValidator validator) {
        this.validator = validator;
    }

    @Override
    public void handle(Context context) {
        Set<UUID> uuidValues = new LinkedHashSet<>();

        FluentLoggingBuilder logger = FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "handle")
                .withField(METHOD, METHOD_GET)
                .withField(HOST, context.getUriInfo().getAbsolutePath().getHost())
                .withField(FluentLoggingUtils.USER_AGENT, context.getHttpHeaders().getHeaderString(USER_AGENT))
                .withField(ACCEPT, APPLICATION_JSON_TYPE);

        Set<String> uuidsForLogging = emptySet();
        Set<String> exceptionMessages = emptySet();

        for (String uuid : context.getUuids()) {
            try {
                validator.validate(uuid);
                uuidValues.add(UUID.fromString(uuid));
            } catch (ValidationException e) {
                uuidsForLogging.add(uuid);
                exceptionMessages.add(e.getMessage());
            }
        }

        if (!exceptionMessages.isEmpty()) {
            logger.withField(MESSAGE,
                    "Invalid uuids=" + uuidsForLogging.toString() + "exceptionMessage=" + exceptionMessages.toString())
                    .build().logInfo();
        }

        context.setValidatedUuids(uuidValues);
    }
}
