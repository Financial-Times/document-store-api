package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static java.lang.String.valueOf;

import java.util.ArrayList;


public class FindMultipleResourcesByUuidsTarget implements Target {

    private MongoDocumentStoreService documentStoreService;

    public FindMultipleResourcesByUuidsTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        return new ArrayList<>(documentStoreService.findByUuids(context.getCollection(), context.getValidatedUuids(),
                valueOf(context.getParameter(METHOD))));
    }
}
