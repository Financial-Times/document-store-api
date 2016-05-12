package com.ft.universalpublishing.documentstore.service;

import java.util.Map;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.write.DocumentWritten;

public interface DocumentStoreService {
  static final String CONTENT_COLLECTION = "content";
  static final String LISTS_COLLECTION = "lists";

    Map<String, Object> findByUuid(String resourceType, UUID fromString);

    Map<String, Object> findByIdentifier(String resourceType, String authority, String identifierValue);
    
    Map<String, Object> findByConceptAndType(String resourceType, String conceptId, String typeId);

    DocumentWritten write(String resourceType, Map<String, Object> content);

    void delete(String resourceType, UUID fromString);

    void applyIndexes();
}
