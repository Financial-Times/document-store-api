package com.ft.universalpublishing.documentstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.clients.PublicConcordancesApiClient;
import com.ft.universalpublishing.documentstore.health.HealthcheckService;
import com.ft.universalpublishing.documentstore.model.read.Concordance;
import com.ft.universalpublishing.documentstore.model.read.Concordances;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicConcordancesApiServiceImpl implements PublicConcordancesApiService, HealthcheckService {
    PublicConcordancesApiClient publicConcordancesApiClient;

    @Override
    public boolean isHealthcheckOK() {
        final Response response = publicConcordancesApiClient.getHealthcheck();
        return response.getStatus() == Response.Status.OK.getStatusCode() ? true : false;
    }

    @Override
    public List<Concordance> getUPPConcordances(String conceptUUID)
            throws JsonMappingException, JsonProcessingException {
        List<Concordance> filteredConcordances = new ArrayList<>();

        if (conceptUUID == null || conceptUUID.isEmpty()) {
            return filteredConcordances;
        }

        Response response = publicConcordancesApiClient.getConcordances(conceptUUID);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            final String payload = response.readEntity(String.class);
            Concordances concordances = new ObjectMapper().readValue(payload, Concordances.class);

            if (concordances != null && concordances.getConcordances() != null) {
                // get only concordances that have UPP-compliant UUIDs
                filteredConcordances = concordances.getConcordances().stream()
                        .filter(concordance -> concordance.getIdentifier().getAuthority().endsWith("/system/UPP"))
                        .collect(Collectors.toList());
            }
        }

        return filteredConcordances;
    }
}
