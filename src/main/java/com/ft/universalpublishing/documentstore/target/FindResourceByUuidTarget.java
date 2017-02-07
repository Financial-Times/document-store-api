package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import java.util.ArrayList;
import java.util.UUID;

public class FindResourceByUuidTarget implements Target {

    private MongoDocumentStoreService documentStoreService;

    public FindResourceByUuidTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        if (context.getUuids().size() > 1) {
            return new ArrayList<>(documentStoreService.findByUuids(context.getCollection(), context.getValidatedUuids()));
        }
        return documentStoreService.findByUuid(context.getCollection(), UUID.fromString(context.getUuid()));
    }
}
