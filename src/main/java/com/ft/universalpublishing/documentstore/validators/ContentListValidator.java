package com.ft.universalpublishing.documentstore.validators;

import com.ft.universalpublishing.documentstore.exception.ValidationException;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.ListItem;

import com.google.common.base.Strings;

import java.util.List;


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
        Concept concept = contentList.getConcept();
        if (concept != null) {
            if (concept.getUuid() == null) {
                throw new ValidationException("if a concept is supplied it must have a non-empty uuid field");
            }
            if (Strings.isNullOrEmpty(concept.getPrefLabel())) {
                throw new ValidationException("if a concept is supplied it must have a non-empty prefLabel field");
            }
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
