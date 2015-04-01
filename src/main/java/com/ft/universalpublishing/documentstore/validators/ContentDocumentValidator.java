package com.ft.universalpublishing.documentstore.validators;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.universalpublishing.documentstore.model.Content;

public class ContentDocumentValidator implements DocumentValidator {

    @Override
    public void validate(String uuidString, Object document) {
        Content content = null;

        if (document == null) {
            throw ClientError.status(400).error("some content must be submitted").exception();
        }        
        if (document instanceof Content) {
            content = (Content) document;
        } else {
            throw ServerError.status(500).error("bad configuration - calling content validator for something that isn't content").exception();
        }
        if (content.getUuid() == null) {
            throw ClientError.status(400).error("submitted content must provide a uuid").exception();
        }
        if (content.getTitle() == null || content.getTitle().isEmpty()) {
            throw ClientError.status(400).error("submitted content must provide a non-empty title").exception();
        }
        if (content.getPublishedDate() == null ) {
            throw ClientError.status(400).error("submitted content must provide a non-empty publishedDate").exception();
        }
        if (!uuidString.equals(content.getUuid())) {
            String message = String.format("uuid in path %s is not equal to uuid in submitted content %s", uuidString, content.getUuid());
            throw ClientError.status(400).error(message).exception();

        }
    }

}
