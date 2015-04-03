package com.ft.universalpublishing.documentstore.validators;

import com.ft.universalpublishing.documentstore.model.Document;

public interface DocumentValidator {

    void validate(String uuidString, Document document);

}
