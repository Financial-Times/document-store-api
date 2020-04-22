package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;

public class UuidValidationHandler implements Handler {

    private UuidValidator validator;

    public UuidValidationHandler(UuidValidator validator) {
        this.validator = validator;
    }

    @Override
    public void handle(Context context) {
        validator.validate(context.getUuid(), "uuid");
    }

}
