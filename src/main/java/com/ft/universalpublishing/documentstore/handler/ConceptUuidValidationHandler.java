package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConceptUuidValidationHandler implements Handler {

    UuidValidator validator;

    @Override
    public void handle(final Context context) {
        final String conceptUUID = context.getConceptUUID();

        if (!Strings.isNullOrEmpty(conceptUUID)) {
            validator.validate(context.getConceptUUID(), "conceptUUID");
        }
    }

}
