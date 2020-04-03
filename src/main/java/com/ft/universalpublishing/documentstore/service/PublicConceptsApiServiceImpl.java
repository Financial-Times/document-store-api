package com.ft.universalpublishing.documentstore.service;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.clients.PublicConceptsApiClient;
import com.ft.universalpublishing.documentstore.health.HealthcheckService;
import com.ft.universalpublishing.documentstore.model.read.Concept;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * PublicConceptsApiServiceImpl
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicConceptsApiServiceImpl implements PublicConceptsApiService, HealthcheckService {
    PublicConceptsApiClient publicConceptsApiClient;

    @Override
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

    @Override
    public Concept getUpToDateConcept(Concept concept) throws JsonMappingException, JsonProcessingException {
        String conceptUUID = concept.getId().getPath().split("/")[2];
        Response response = publicConceptsApiClient.getConcept(conceptUUID);
        final String payload = response.readEntity(String.class);
        Concept upToDateConcept = new ObjectMapper().reader().forType(Concept.class).readValue(payload);

        return upToDateConcept;
    }

    @Override
    public List<Concept> searchConcepts(String[] conceptUUIDs) throws JsonMappingException, JsonProcessingException {
        Response response = publicConceptsApiClient.searchConcepts(conceptUUIDs);
        final String payload = response.readEntity(String.class);
        List<Concept> concepts = Arrays.asList(new ObjectMapper().readValue(payload, Concept[].class));

        return concepts;
    }
}
