package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;

import static com.ft.api.jaxrs.errors.ClientError.status;

import java.util.List;

public class ExtractUuidsHandler implements Handler {

    @Override
    public void handle(Context context) {
        List<String> uuids = context.getUriInfo().getQueryParameters().get("uuid");
        if (uuids == null){
            throw status(400).error("Invalid request (missing \"uuid\" parameter)").exception();
        }
        context.setUuids(uuids);
    }
}
