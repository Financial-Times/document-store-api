package com.ft.universalpublishing.documentstore.resources;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.universalpublishing.documentstore.exception.ExternalSystemUnavailableException;
import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;

import java.util.UUID;

public abstract class AbstractResource {

    protected static final String CHARSET_UTF_8 = ";charset=utf-8";
    protected static final String CONTENT_COLLECTION = "content";

    private DocumentStoreService documentStoreService;
    private UuidValidator uuidValidator;

    public AbstractResource(final DocumentStoreService documentStoreService,
                            final UuidValidator uuidValidator) {
        this.documentStoreService = documentStoreService;
        this.uuidValidator = uuidValidator;
    }

    protected void validateUuid(String uuidString) {
        try {
            uuidValidator.validate(uuidString);
        } catch (ValidationException validationException) {
            throw ClientError.status(400).error(validationException.getMessage()).exception();
        }
    }

    protected com.mongodb.DBObject findResourceByUuid(final String resourceType, final String uuid) {
        throw new RuntimeException("Not implemented.");
    }

    protected <T extends Document> T findResourceByUuid(final String resourceType, final String uuid, final Class<T> documentClass) {
        try {
            final T foundDocument = documentStoreService.findByUuid(resourceType, UUID.fromString(uuid), documentClass);
            if (foundDocument!= null) {
                return foundDocument;
            } else {
                throw ClientError.status(404).error("Requested item does not exist").exception();
            }
        } catch (ExternalSystemUnavailableException esue) {
            throw ServerError.status(503).error("upstream system unavailable").exception(esue);
        }
    }
}
