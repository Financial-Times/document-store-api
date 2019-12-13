package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import static com.ft.universalpublishing.documentstore.utils.FluentLoggingUtils.METHOD_DELETE;
import static java.util.UUID.fromString;
import static javax.ws.rs.core.Response.ok;

public class DeleteDocumentTarget implements Target {

    private MongoDocumentStoreService documentStoreService;

    public DeleteDocumentTarget(
            MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

    @Override
    public Object execute(Context context) {
        try {
            documentStoreService.delete(context.getCollection(),
                    fromString(context.getUuid()), METHOD_DELETE);
            return ok().build();
        } catch (DocumentNotFoundException e) {
            return ok().build();
        }
    }
}
