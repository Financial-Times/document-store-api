package com.ft.universalpublishing.documentstore.validators;

import com.ft.universalpublishing.documentstore.model.ContentList;

public interface DocumentValidator {

    void validate(String uuidString, ContentList document);

}
