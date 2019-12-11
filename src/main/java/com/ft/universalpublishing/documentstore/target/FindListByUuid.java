package com.ft.universalpublishing.documentstore.target;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import java.util.Map;

import static com.ft.api.jaxrs.errors.ClientError.status;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.STATUS;
import static java.lang.String.valueOf;
import static java.util.UUID.fromString;
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
        Map<String, Object> contentMap = documentStoreService.findByUuid(context.getCollection(),
                fromString(context.getUuid()), valueOf(context.getParameter(METHOD)));
        try {
            ContentList contentList = new ObjectMapper().convertValue(contentMap, ContentList.class);
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
