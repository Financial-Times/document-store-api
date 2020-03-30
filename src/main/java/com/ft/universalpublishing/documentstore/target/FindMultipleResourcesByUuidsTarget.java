package com.ft.universalpublishing.documentstore.target;

import java.util.ArrayList;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindMultipleResourcesByUuidsTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        return new ArrayList<>(documentStoreService.findByUuids(context.getCollection(), context.getValidatedUuids()));
    }
}
