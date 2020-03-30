package com.ft.universalpublishing.documentstore.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
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
                isOK = jsonNode.at("/ok").asBoolean();
            } catch (final JsonProcessingException e) {
                isOK = false;
            }
        }
        return isOK;
    }

    @Override
    public Concept getUpToDateConcept(String conceptUUID) throws JsonMappingException, JsonProcessingException {
        if (conceptUUID == null || conceptUUID.isEmpty()) {
            return null;
        }

        Response response = publicConceptsApiClient.getConcept(conceptUUID);
        Concept upToDateConcept = null;

        if (response.getStatus() == HttpServletResponse.SC_OK) {
            final String payload = response.readEntity(String.class);
            upToDateConcept = new ObjectMapper().reader().forType(Concept.class).readValue(payload);
        }

        return upToDateConcept;
    }

    @Override
    public List<Concept> searchConcepts(String[] conceptUUIDs) throws JsonMappingException, JsonProcessingException {
        List<Concept> concepts = new ArrayList<>();

        if (conceptUUIDs == null || conceptUUIDs.length == 0) {
            return concepts;
        }

        Response response = publicConceptsApiClient.searchConcepts(conceptUUIDs);

        if (response.getStatus() == HttpServletResponse.SC_OK) {
            final String payload = response.readEntity(String.class);
            concepts = Arrays.asList(new ObjectMapper().readValue(payload, Concept[].class));
        }

        return concepts;
    }
}
