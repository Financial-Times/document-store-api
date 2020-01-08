package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

@RequiredArgsConstructor
public class FindAllTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        return Collections.singletonList(documentStoreService.findAll(context.getCollection()));
    }
}