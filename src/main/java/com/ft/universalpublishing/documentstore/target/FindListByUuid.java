package com.ft.universalpublishing.documentstore.target;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import lombok.RequiredArgsConstructor;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;

import java.util.Map;

import static com.ft.api.jaxrs.errors.ClientError.status;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.STATUS;
import static java.util.UUID.fromString;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
public class FindListByUuid implements Target {

    private final MongoDocumentStoreService documentStoreService;

    private final String apiPath;

    @Override
    public Object execute(Context context) {
        Map<String, Object> contentMap = documentStoreService.findByUuid(context.getCollection(),
                fromString(context.getUuid()));
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
