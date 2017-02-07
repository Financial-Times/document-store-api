package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.exception.DocumentNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;

import java.util.UUID;

import javax.ws.rs.core.Response;


public class DeleteDocumentTarget implements Target {

    private MongoDocumentStoreService documentStoreService;

    public DeleteDocumentTarget(
            MongoDocumentStoreService documentStoreService) {
        this.documentStoreService = documentStoreService;
    }

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
