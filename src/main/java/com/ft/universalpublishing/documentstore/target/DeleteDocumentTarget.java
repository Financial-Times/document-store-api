package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import javax.ws.rs.core.Response;

@RequiredArgsConstructor
public class DeleteDocumentTarget implements Target {

    private final MongoDocumentStoreService documentStoreService;

    @Override
    public Object execute(Context context) {
        try {
            documentStoreService.delete(context.getCollection(), UUID.fromString(context.getUuid()));
            return Response.ok().build();
        } catch (DocumentNotFoundException e) {
            return Response.ok().build();
        }
    }
}
