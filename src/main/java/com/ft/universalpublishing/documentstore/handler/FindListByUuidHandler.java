package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindListByUuidHandler implements Handler {

  private final MongoDocumentStoreService documentStoreService;

  @Override
  public void handle(Context context) {
    Map<String, Object> contentMap =
        documentStoreService.findByUuid(
            context.getCollection(), UUID.fromString(context.getUuid()));
    context.setContentMap(contentMap);
  }
}
