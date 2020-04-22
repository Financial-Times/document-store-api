package com.ft.universalpublishing.documentstore.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.clients.PublicConcordancesApiClient;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.Concordance;
import com.ft.universalpublishing.documentstore.model.read.Concordances;
import com.ft.universalpublishing.documentstore.model.read.Identifier;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class PublicConcordancesApiServiceImplTest {
    private PublicConcordancesApiClient publicConcordancesApiClientMock = mock(PublicConcordancesApiClient.class);
    private PublicConcordancesApiServiceImpl publicConcordancesApiService = new PublicConcordancesApiServiceImpl(
            publicConcordancesApiClientMock);

    @BeforeEach
    public void setup() {
        reset(publicConcordancesApiClientMock);
    }

    @Test
    public void healthcheckIsOK() {
        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());

        when(publicConcordancesApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConcordancesApiService.isHealthcheckOK();
        assertTrue(isHealthcheckOK);
    }

    @Test
    public void healthcheckIsNotOK() {
        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        when(publicConcordancesApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConcordancesApiService.isHealthcheckOK();
        assertFalse(isHealthcheckOK);
    }

    @Test
    public void shouldNotSearchConceptsInvokedWithNull() throws JsonProcessingException {
        List<Concordance> result = publicConcordancesApiService.getUPPConcordances(null);
        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldNotSearchConceptsInvokedWithEmptyArray() throws JsonProcessingException {
        List<Concordance> result = publicConcordancesApiService.getUPPConcordances("");
        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldNotSearchConcepts500StatusCode() throws JsonProcessingException {
        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        String conceptUUID = "123";
        when(publicConcordancesApiClientMock.getConcordances(eq(conceptUUID))).thenReturn(response);
        List<Concordance> result = publicConcordancesApiService.getUPPConcordances(conceptUUID);
        verify(publicConcordancesApiClientMock).getConcordances(eq(conceptUUID));

        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldNotSearchConcepts404StatusCode() throws JsonProcessingException {
        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());

        String conceptUUID = "123";
        when(publicConcordancesApiClientMock.getConcordances(eq(conceptUUID))).thenReturn(response);
        List<Concordance> result = publicConcordancesApiService.getUPPConcordances(conceptUUID);
        verify(publicConcordancesApiClientMock).getConcordances(eq(conceptUUID));

        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldSearchConcepts() throws JsonProcessingException {
        Concept concept = new Concept(UUID.randomUUID(), "somePrefLabel");
        Concept concept2 = new Concept(UUID.randomUUID(), "somePrefLabel2");
        Concept concept3 = new Concept(UUID.randomUUID(), "somePrefLabel3");
        Identifier identifier = new Identifier("http://api.ft.com/system/UPP", UUID.randomUUID().toString());
        Identifier identifier2 = new Identifier("http://api.ft.com/system/UPP", UUID.randomUUID().toString());
        Identifier identifier3 = new Identifier("http://api.ft.com/system/SMARTLOGIC", UUID.randomUUID().toString());

        Concordance concordance = new Concordance(concept, identifier);
        Concordance concordance2 = new Concordance(concept2, identifier2);
        Concordance concordance3 = new Concordance(concept3, identifier3);
        Concordances concordances = new Concordances(Arrays.asList(concordance, concordance2, concordance3));

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(concordances);

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        String conceptUUID = "123";
        when(publicConcordancesApiClientMock.getConcordances(eq(conceptUUID))).thenReturn(response);
        List<Concordance> result = publicConcordancesApiService.getUPPConcordances(conceptUUID);
        verify(publicConcordancesApiClientMock).getConcordances(eq(conceptUUID));

        assertTrue(result.size() == 2);
    }
}
