package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import lombok.RequiredArgsConstructor;

import static java.util.UUID.fromString;
import static javax.ws.rs.core.Response.ok;

@RequiredArgsConstructor
public class DeleteDocumentTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        try {
            documentStoreService.delete(context.getCollection(), fromString(context.getUuid()));
            return ok().build();
        } catch (DocumentNotFoundException e) {
            return ok().build();
        }
    }
}
