package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;

import java.util.List;

public class ExtractUuidsHandler implements Handler {

    @Override
    public void handle(Context context) {
        List<String> uuids = context.getUriInfo().getQueryParameters().get("uuid");
        context.setUuids(uuids);
    }
}
