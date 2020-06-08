package com.ft.universalpublishing.documentstore.target;

import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bson.Document;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FilterContentTarget implements Target {
  private final MongoDocumentStoreService documentStoreService;

  @Override
  public Object execute(Context context) {
    UUID[] conceptUUIDs = (UUID[]) context.getParameter("conceptUUIDs");
    List<Document> listDocuments =
        documentStoreService.filterCollection(
            context.getCollection(),
            conceptUUIDs,
            context.getListType(),
            context.getSearchTerm(),
            context.getWebUrl(),
            context.getStandfirst());

    return listDocuments;
  }
}
