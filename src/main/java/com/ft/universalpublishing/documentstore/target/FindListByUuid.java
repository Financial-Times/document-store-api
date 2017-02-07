package com.ft.universalpublishing.documentstore.target;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import java.util.Map;
import java.util.UUID;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class FindListByUuid implements Target {

    private MongoDocumentStoreService documentStoreService;

    private String apiPath;

    public FindListByUuid(MongoDocumentStoreService documentStoreService, String apiPath) {
        this.documentStoreService = documentStoreService;
        this.apiPath = apiPath;
    }

    @Override
    public Object execute(Context context) {
        Map<String, Object> contentMap = documentStoreService
                .findByUuid(context.getCollection(), UUID.fromString(context.getUuid()));
        try {
            ContentList contentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
            contentList.addIds();
            contentList.addApiUrls(apiPath);
            contentList.removePrivateFields();
            return contentList;
        } catch (IllegalArgumentException e) {
            throw ClientError.status(SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
    }
}
