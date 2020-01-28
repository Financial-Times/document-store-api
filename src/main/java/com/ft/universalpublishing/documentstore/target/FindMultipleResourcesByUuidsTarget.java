package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class FindMultipleResourcesByUuidsTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        return new ArrayList<>(documentStoreService.findByUuids(context.getCollection(), context.getValidatedUuids()));
    }
}
