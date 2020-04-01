package com.ft.universalpublishing.documentstore.service;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.clients.PublicConceptsApiClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * PublicConceptsApiServiceImpl
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicConceptsApiServiceImpl implements PublicConceptsApiService {
    PublicConceptsApiClient publicConceptsApiClient;

    public boolean isHealthcheckOK() {
        final Response response = publicConceptsApiClient.getHealthcheck();

        Boolean isOK = null;

        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            final String payload = response.readEntity(String.class);
            JsonNode jsonNode;
            try {
                jsonNode = new ObjectMapper().readValue(payload, JsonNode.class);
                isOK = jsonNode.at("/checks/0/ok").asBoolean();
            } catch (final JsonProcessingException e) {
                isOK = false;
            }
        }
        return isOK;
    }
}
