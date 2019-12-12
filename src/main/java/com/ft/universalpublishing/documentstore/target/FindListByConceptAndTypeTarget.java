package com.ft.universalpublishing.documentstore.target;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import java.util.Map;
import java.util.UUID;

import static com.ft.api.jaxrs.errors.ClientError.status;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_GET;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.STATUS;
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

        Map<String, Object> result = documentStoreService.findByConceptAndType(context.getCollection(), conceptId,
                listType, METHOD_GET);
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
            WebApplicationClientException clientException = status(SC_INTERNAL_SERVER_ERROR).error(e.getMessage())
                    .exception();
            new FluentLoggingWrapper().withClassName(this.getClass().getCanonicalName()).withMetodName("execute")
                    .withField(STATUS, SC_INTERNAL_SERVER_ERROR).withException(clientException).build().logWarn();

            throw clientException;
        }
    }
}
