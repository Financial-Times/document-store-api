package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

public class FindIDsTarget implements Target{

    private MongoDocumentStoreService documentStoreService;

    public FindIDsTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        return documentStoreService.findUUIDs(context.getCollection());
    }
}
