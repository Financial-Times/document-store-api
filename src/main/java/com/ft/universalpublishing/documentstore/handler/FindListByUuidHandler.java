package com.ft.universalpublishing.documentstore.handler;

import java.util.Map;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindListByUuidHandler implements Handler {

    private final MongoDocumentStoreService documentStoreService;
    private final String apiPath;

    @Override
    public void handle(Context context) {
        Map<String, Object> contentMap = documentStoreService.findByUuid(context.getCollection(),
                UUID.fromString(context.getUuid()));
        // try {
        // ContentList contentList = new ObjectMapper().convertValue(contentMap,
        // ContentList.class);
        // contentList.addIds();
        // contentList.addApiUrls(apiPath);
        // contentList.removePrivateFields();
        context.setContentMap(contentMap);
        // } catch (IllegalArgumentException e) {
        // throw
        // ClientError.status(SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        // }
    }
}
