package com.ft.universalpublishing.documentstore.handler;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public class FilterListsHandler implements Handler {

  private final MongoDocumentStoreService documentStoreService;

  @Override
  public void handle(Context context) {
    UUID[] conceptUUIDs = (UUID[]) context.getParameter("conceptUUIDs");
    List<Document> listDocuments =
        documentStoreService.filterCollection(
            context.getCollection(),
            conceptUUIDs,
            context.getListType(),
            context.getSearchTerm(),
            context.getWebUrl(),
            context.getStandfirst());

    context.setDocuments(listDocuments);
  }
}
