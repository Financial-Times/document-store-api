package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static java.lang.String.valueOf;
import static java.util.UUID.fromString;

public class FindResourceByUuidTarget implements Target {

    private MongoDocumentStoreService documentStoreService;

    public FindResourceByUuidTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        return documentStoreService.findByUuid(context.getCollection(), fromString(context.getUuid()),
                valueOf(context.getParameter(METHOD)));
    }
}
