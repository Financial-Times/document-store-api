package com.ft.universalpublishing.documentstore.clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
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
    String host;
    Client client;

    public Response getHealthcheck() {
        return client.target(String.format("http://%s/__health", host)).request(MediaType.APPLICATION_JSON).get();
    }

    public Response getConcept(String conceptUUID) {
        return client.target(String.format("http://%s/concepts/%s", host, conceptUUID))
                .request(MediaType.APPLICATION_JSON).get();
    }

    public Response searchConcepts(String[] conceptUUIDs) {
        return client.target(String.format("http://%s/concepts", host)).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(conceptUUIDs, MediaType.APPLICATION_JSON));
    }

}
