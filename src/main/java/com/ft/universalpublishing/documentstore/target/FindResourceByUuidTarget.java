package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import java.util.UUID;

public class FindResourceByUuidTarget implements Target {

    private MongoDocumentStoreService documentStoreService;

    public FindResourceByUuidTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        return documentStoreService.findByUuid(context.getCollection(), UUID.fromString(context.getUuid()));
    }
}
