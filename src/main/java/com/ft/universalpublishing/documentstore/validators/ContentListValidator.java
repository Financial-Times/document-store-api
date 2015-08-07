package com.ft.universalpublishing.documentstore.validators;

import java.util.List;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.ContentList;
import com.ft.universalpublishing.documentstore.model.ListItem;


public class ContentListValidator {

    private UuidValidator uuidValidator;

    public ContentListValidator(UuidValidator uuidValidator) {
        this.uuidValidator = uuidValidator;
    }

    public void validate(String uuidString, ContentList contentList) {
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
        List<ListItem> items = contentList.getItems();
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
