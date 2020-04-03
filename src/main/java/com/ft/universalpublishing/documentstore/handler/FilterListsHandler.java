package com.ft.universalpublishing.documentstore.handler;

import java.util.List;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import org.bson.Document;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FilterListsHandler implements Handler {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public void handle(Context context) {
        UUID[] conceptUUIDs = (UUID[]) context.getParameter("conceptUUIDs");
        List<Document> listDocuments = documentStoreService.filterLists(context.getCollection(), conceptUUIDs,
                context.getListType(), context.getSearchTerm());

        context.addParameter("listDocuments", listDocuments);
    }
}
