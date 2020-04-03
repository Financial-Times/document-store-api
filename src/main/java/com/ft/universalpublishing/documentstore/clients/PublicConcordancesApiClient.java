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
public class PublicConcordancesApiClient {
    String host;
    Client client;

    public Response getHealthcheck() {
        return client.target(String.format("http://%s/__health", host)).request(MediaType.APPLICATION_JSON).get();
    }

    public Response getConcordances(String conceptUUID) {
        // StringBuilder urlBuilder = new
        // StringBuilder("http://").append(host).append("/concordances?");
        // for (int i = 0; i < conceptUUIDs.length; i++) {
        // urlBuilder = urlBuilder.append("conceptId=").append(conceptUUIDs[i]);

        // if (i > 0 && i < conceptUUIDs.length - 1) {
        // urlBuilder = urlBuilder.append("&");
        // }
        // }

        return client.target(String.format("http://%s/concordances?conceptId=%s", host, conceptUUID))
                .request(MediaType.APPLICATION_JSON).get();

        // return
        // client.target(urlBuilder.toString()).request(MediaType.APPLICATION_JSON).get();
    }

}
