package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;

public class ConceptUuidValidationHandler implements Handler {

    private final UuidValidator validator;

    public ConceptUuidValidationHandler(final UuidValidator validator) {
        this.validator = validator;
    }

    @Override
    public void handle(final Context context) {
        final String conceptUUID = context.getConceptUUID();

        if (conceptUUID != null && !conceptUUID.isEmpty()) {
            validator.validate(context.getConceptUUID(), "conceptUUID");
        }
    }

}
