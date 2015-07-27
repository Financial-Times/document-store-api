package com.ft.universalpublishing.documentstore.service;

import java.util.Map;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;

public interface DocumentStoreService {

    Map<String, Object> findByUuid(String resourceType, UUID fromString);

    DocumentWritten write(String resourceType, Map<String, Object> content);

    void delete(String resourceType, UUID fromString);

}
