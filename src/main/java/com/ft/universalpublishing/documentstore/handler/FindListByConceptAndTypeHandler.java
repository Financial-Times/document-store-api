package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.exception.DocumentsNotFoundException;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FindListByConceptAndTypeHandler implements Handler {

  private final MongoDocumentStoreService documentStoreService;

  @Override
  public void handle(final Context context) {
    final UUID[] conceptUUIDs = (UUID[]) context.getParameter("conceptUUIDs");
    final String listType = (String) context.getParameter("listType");

    final Map<String, Object> result =
        Optional.ofNullable(
                documentStoreService.findByConceptAndType(
                    context.getCollection(), conceptUUIDs, listType))
            .orElseThrow(() -> new DocumentsNotFoundException(conceptUUIDs));

    context.setContentMap(result);
  }
}
