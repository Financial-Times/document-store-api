package com.ft.universalpublishing.documentstore.handler;

import java.util.List;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import org.bson.Document;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindMultipleResourcesByUuidsHandler implements Handler {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public void handle(Context context) {
        List<Document> documents = documentStoreService.findByUuids(context.getCollection(),
                context.getValidatedUuids());
        context.setDocuments(documents);
    }
}
