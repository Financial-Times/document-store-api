package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindResourceByUuidTarget implements Target {

  private final MongoDocumentStoreService documentStoreService;

  @Override
  public Object execute(Context context) {
    return documentStoreService.findByUuid(
        context.getCollection(), UUID.fromString(context.getUuid()));
  }
}
