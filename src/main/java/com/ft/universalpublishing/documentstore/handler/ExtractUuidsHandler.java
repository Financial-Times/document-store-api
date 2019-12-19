package com.ft.universalpublishing.documentstore.handler;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.model.read.Context;

import java.util.List;

public class ExtractUuidsHandler implements Handler {

    @Override
    public void handle(Context context) {
        List<String> uuids = context.getUriInfo().getQueryParameters().get("uuid");
        if (uuids == null){
            throw ClientError.status(400).error("Invalid request (missing \"uuid\" parameter)").exception();
        }
        context.setUuids(uuids);
    }
}
