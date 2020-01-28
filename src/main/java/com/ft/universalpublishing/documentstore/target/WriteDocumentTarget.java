package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.core.Response;

@RequiredArgsConstructor
public class WriteDocumentTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        final DocumentWritten written = documentStoreService.write(context.getCollection(), context.getContentMap());
        final Response response;
        switch (written.getMode()) {
            case Created:
                response = Response.created(context.getUriInfo().getRequestUri()).build();
                break;
            case Updated:
                response = Response.ok(written.getDocument()).build();
                break;
            default:
                throw new IllegalStateException("unknown write mode " + written.getMode());
        }
        return response;
    }
}
