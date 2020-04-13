package com.ft.universalpublishing.documentstore.clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicConcordancesApiClient {
    String host;
    Client client;

    public Response getHealthcheck() {
        return client.target(String.format("http://%s/__gtg", host)).request(MediaType.APPLICATION_JSON).get();
    }

    public Response getConcordances(String conceptUUID) {
        return client.target(String.format("http://%s/concordances?conceptId=%s", host, conceptUUID))
                .request(MediaType.APPLICATION_JSON).get();
    }

}
