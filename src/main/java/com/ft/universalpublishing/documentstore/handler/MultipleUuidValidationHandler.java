package com.ft.universalpublishing.documentstore.handler;


import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;

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
        for (String uuid : context.getUuids()) {
            try {
                validator.validate(uuid);
                uuidValues.add(UUID.fromString(uuid));
            } catch (ValidationException e) {
               /* ignore */
            }
        }
        context.setValidatedUuids(uuidValues);
    }
}
