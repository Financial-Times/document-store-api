package com.ft.universalpublishing.documentstore.validators;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.Document;


public class ContentListDocumentValidator implements DocumentValidator {

    @Override
    public void validate(String uuidString, Document document) {
        ContentList contentList = (ContentList) document;
        if (contentList == null) {
            throw new ValidationException("list must be provided in request body");
        }
        if (contentList.getUuid() == null || contentList.getUuid().isEmpty()) {
            throw new ValidationException("submitted list must provide a non-empty uuid");
        }
        if (contentList.getTitle() == null || contentList.getTitle().isEmpty()) {
            throw new ValidationException("submitted list must provide a non-empty title");
        }
        if (!uuidString.equals(contentList.getUuid())) {
            String message = String.format("uuid in path %s is not equal to uuid in submitted list %s", uuidString, contentList.getUuid());
            throw new ValidationException(message);

        }
    }

}
