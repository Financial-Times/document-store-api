package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.utils.FluentLoggingWrapper;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD;
import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.TRANSACTION_ID;
import static java.lang.String.valueOf;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.ok;
import static org.slf4j.MDC.get;

import javax.ws.rs.core.Response;

public class WriteDocumentTarget implements Target {


    private MongoDocumentStoreService documentStoreService;

    public WriteDocumentTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        final DocumentWritten written = documentStoreService.write(context.getCollection(), context.getContentMap(),
                valueOf(context.getParameter(METHOD)));
        final Response response;
        switch (written.getMode()) {
            case Created:
                response = created(context.getUriInfo().getRequestUri()).build();
                break;
            case Updated:
                response = ok(written.getDocument()).build();
                break;
            default:
                throw new IllegalStateException("unknown write mode " + written.getMode());
        }
        
        new FluentLoggingWrapper().withClassName(this.getClass().getCanonicalName())
                .withMetodName("execute").withResponse(response)
                .withTransactionId(get(TRANSACTION_ID)).withField(METHOD, valueOf(context.getParameter(METHOD)))
                .build().logInfo();

        return response;
    }
}
