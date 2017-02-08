package com.ft.universalpublishing.documentstore.target;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import java.util.Map;
import java.util.UUID;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;


public class FindListByConceptAndTypeTarget implements Target {

    private MongoDocumentStoreService documentStoreService;
    private String apiPath;

    public FindListByConceptAndTypeTarget(MongoDocumentStoreService documentStoreService,
                                          String apiPath) {
        this.documentStoreService = documentStoreService;
        this.apiPath = apiPath;
    }

    @Override
    public Object execute(Context context) {
        UUID conceptId = (UUID) context.getParameter("conceptId");
        String listType = (String) context.getParameter("listType");

        Map<String, Object> result = documentStoreService
                .findByConceptAndType(context.getCollection(), conceptId, listType);
        if (result == null) {
            throw new DocumentNotFoundException(conceptId);
        }
        try {
            ContentList contentList = new ObjectMapper().convertValue(result, ContentList.class);
            contentList.addIds();
            contentList.addApiUrls(apiPath);
            contentList.removePrivateFields();
            return contentList;
        } catch (IllegalArgumentException e) {
            throw ClientError.status(SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
    }
}
