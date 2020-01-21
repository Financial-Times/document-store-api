package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FilterListsTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        return documentStoreService.filterLists(
                context.getCollection(),
                context.getConceptUUID(),
                context.getListType(),
                context.getSearchTerm()
        );
    }
}
