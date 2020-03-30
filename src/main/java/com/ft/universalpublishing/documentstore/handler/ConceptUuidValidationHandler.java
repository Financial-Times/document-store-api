package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;

public class ConceptUuidValidationHandler implements Handler {

    private UuidValidator validator;

    public ConceptUuidValidationHandler(UuidValidator validator) {
        this.validator = validator;
    }

    @Override
    public void handle(Context context) {
        validator.validate(context.getConceptUUID(), "conceptUUID");
    }

}
