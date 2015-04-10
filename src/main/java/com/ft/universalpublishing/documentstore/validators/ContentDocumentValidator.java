package com.ft.universalpublishing.documentstore.validators;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.Content;
import com.ft.universalpublishing.documentstore.model.Document;

public class ContentDocumentValidator implements DocumentValidator {

    public void validate(String uuidString, Document document) {
        Content content = (Content) document;   
        
        if (content == null) {
            throw new ValidationException("content must be provided in request body");
        }
        if (content.getUuid() == null || content.getUuid().isEmpty()) {
            throw new ValidationException("submitted content must provide a non-empty uuid");
        }
        if (content.getTitle() == null || content.getTitle().isEmpty()) {
            throw new ValidationException("submitted content must provide a non-empty title");
        }
        if (content.getPublishedDate() == null) {
            throw new ValidationException("submitted content must provide a non-empty publishedDate");
        }
        if (!uuidString.equals(content.getUuid())) {
            String message = String.format("uuid in path %s is not equal to uuid in submitted content %s", uuidString, content.getUuid());
            throw new ValidationException(message);

        }
    }

}
