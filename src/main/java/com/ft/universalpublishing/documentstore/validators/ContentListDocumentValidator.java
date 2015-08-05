package com.ft.universalpublishing.documentstore.validators;

import java.util.List;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.ListItem;


public class ContentListDocumentValidator implements DocumentValidator {

    private UuidValidator uuidValidator;

    public ContentListDocumentValidator(UuidValidator uuidValidator) {
        this.uuidValidator = uuidValidator;
    }

    @Override
    public void validate(String uuidString, ContentList document) {
        if (document == null) {
            throw new ValidationException("list must be provided in request body");
        }
        if (document.getUuid() == null || document.getUuid().isEmpty()) {
            throw new ValidationException("submitted list must provide a non-empty uuid");
        }
        if (document.getTitle() == null || document.getTitle().isEmpty()) {
            throw new ValidationException("submitted list must provide a non-empty title");
        }
        if (!uuidString.equals(document.getUuid())) {
            String message = String.format("uuid in path %s is not equal to uuid in submitted list %s", uuidString, document.getUuid());
            throw new ValidationException(message);

        }
        List<ListItem> items = document.getItems();
        if (items == null) {
            throw new ValidationException("submitted list should have an 'items' field");
        }
        //TODO - when we remove the webUrl support, just make sure each one has a uuid
        for (ListItem item: items) {
            if ((item.getUuid() == null || item.getUuid().isEmpty()) 
                    && (item.getWebUrl() == null || item.getWebUrl().isEmpty())) {
                throw new ValidationException("list items must have a non-empty uuid or a non-empty webUrl");
            }
            String itemUuid = item.getUuid();
            if (itemUuid != null) {
                uuidValidator.validate(itemUuid);
            }
        }
    }

}
