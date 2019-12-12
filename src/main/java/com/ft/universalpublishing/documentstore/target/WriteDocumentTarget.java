package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_POST;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.ok;

import javax.ws.rs.core.Response;

public class WriteDocumentTarget implements Target {


    private MongoDocumentStoreService documentStoreService;

    public WriteDocumentTarget(MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        final DocumentWritten written = documentStoreService.write(context.getCollection(), context.getContentMap(),
                METHOD_POST);
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
        
        return response;
    }
}
