package com.ft.universalpublishing.documentstore.service;

import java.util.Map;
import java.util.UUID;

import com.ft.universalpublishing.documentstore.write.DocumentWritten;
import com.google.common.base.Optional;

public interface DocumentStoreService {

    Optional<Map<String, Object>> findByUuid(String resourceType, UUID fromString);

    DocumentWritten write(String resourceType, Map<String, Object> document);

    void delete(String resourceType, UUID fromString);

}
