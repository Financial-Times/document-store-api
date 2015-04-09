package com.ft.universalpublishing.documentstore.service;

import java.util.UUID;

import com.ft.universalpublishing.documentstore.model.Document;
import com.ft.universalpublishing.documentstore.write.DocumentWritten;

public interface DocumentStoreService {

    <T extends Document> T findByUuid(String resourceType, UUID fromString, Class<T> documentClass);

    <T extends Document> DocumentWritten write(String resourceType, T document, Class<T> documentClass);

    <T extends Document> void delete(String resourceType, UUID fromString, Class<T> documentClass);

}
