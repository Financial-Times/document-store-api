package com.ft.universalpublishing.documentstore.clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * PublicConceptsApiClient
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicConceptsApiClient {
    String baseURL;
    Client client;

    public Response getHealthcheck() {
        return client.target(String.format("%s/__health", baseURL)).request(MediaType.APPLICATION_JSON).get();
    }

}
