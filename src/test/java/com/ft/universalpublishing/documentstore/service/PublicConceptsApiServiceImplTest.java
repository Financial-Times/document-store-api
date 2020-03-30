package com.ft.universalpublishing.documentstore.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.clients.PublicConceptsApiClient;
import com.ft.universalpublishing.documentstore.model.read.Concept;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class PublicConceptsApiServiceImplTest {
    private PublicConceptsApiClient publicConceptsApiClientMock = mock(PublicConceptsApiClient.class);
    private PublicConceptsApiServiceImpl publicConceptApiService = new PublicConceptsApiServiceImpl(
            publicConceptsApiClientMock);

    @BeforeEach
    public void setup() {
        reset(publicConceptsApiClientMock);
    }

    @Test
    public void healthcheckIsOK() {
        String message = "{\"ok\": true}";

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        when(publicConceptsApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConceptApiService.isHealthcheckOK();
        assertTrue(isHealthcheckOK);
    }

    @Test
    public void healthcheckIsNotOK() {
        String message = "{\"ok\": false}";

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        when(publicConceptsApiClientMock.getHealthcheck()).thenReturn(response);
        boolean isHealthcheckOK = publicConceptApiService.isHealthcheckOK();
        assertFalse(isHealthcheckOK);
    }

    @Test
    public void shouldNotGetUpToDateConceptIvnokedWithNull() throws JsonMappingException, JsonProcessingException {
        Concept result = publicConceptApiService.getUpToDateConcept(null);
        assertNull(result);
    }

    @Test
    public void shouldNotGetUpToDateConceptInvokedWithEmptyString()
            throws JsonMappingException, JsonProcessingException {
        Concept result = publicConceptApiService.getUpToDateConcept("");
        assertNull(result);
    }

    @Test
    public void shouldGetUpToDateConcept() throws JsonProcessingException {
        Concept concept = new Concept(UUID.randomUUID(), "somePrefLabel");
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(concept);

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        String conceptUuid = concept.getUuid().toString();
        when(publicConceptsApiClientMock.getConcept(eq(conceptUuid))).thenReturn(response);
        Concept result = publicConceptApiService.getUpToDateConcept(conceptUuid);
        verify(publicConceptsApiClientMock).getConcept(eq(conceptUuid));

        assertEquals(concept, result, "Expected concepts to be equal");
    }

    @Test
    public void shouldNotGetUpToDateConcept500StatusCode() throws JsonProcessingException {
        Concept concept = new Concept(UUID.randomUUID(), "somePrefLabel");

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        String conceptUuid = concept.getUuid().toString();
        when(publicConceptsApiClientMock.getConcept(eq(conceptUuid))).thenReturn(response);
        Concept result = publicConceptApiService.getUpToDateConcept(conceptUuid);
        verify(publicConceptsApiClientMock).getConcept(eq(conceptUuid));

        assertNull(result);
    }

    @Test
    public void shouldNotGetUpToDateConcept404StatusCode() throws JsonProcessingException {
        Concept concept = new Concept(UUID.randomUUID(), "somePrefLabel");

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());

        String conceptUuid = concept.getUuid().toString();
        when(publicConceptsApiClientMock.getConcept(eq(conceptUuid))).thenReturn(response);
        Concept result = publicConceptApiService.getUpToDateConcept(conceptUuid);
        verify(publicConceptsApiClientMock).getConcept(eq(conceptUuid));

        assertNull(result);
    }

    @Test
    public void shouldNotSearchConceptsInvokedWithNull() throws JsonProcessingException {
        List<Concept> result = publicConceptApiService.searchConcepts(null);
        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldNotSearchConceptsInvokedWithEmptyArray() throws JsonProcessingException {
        List<Concept> result = publicConceptApiService.searchConcepts(new String[] {});
        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldNotSearchConcepts500StatusCode() throws JsonProcessingException {
        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        String[] conceptUUIDs = new String[] { "123", "456" };
        when(publicConceptsApiClientMock.searchConcepts(eq(conceptUUIDs))).thenReturn(response);
        List<Concept> result = publicConceptApiService.searchConcepts(conceptUUIDs);
        verify(publicConceptsApiClientMock).searchConcepts(eq(conceptUUIDs));

        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldNotSearchConcepts404StatusCode() throws JsonProcessingException {
        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.NOT_FOUND.getStatusCode());

        String[] conceptUUIDs = new String[] { "123", "456" };
        when(publicConceptsApiClientMock.searchConcepts(eq(conceptUUIDs))).thenReturn(response);
        List<Concept> result = publicConceptApiService.searchConcepts(conceptUUIDs);
        verify(publicConceptsApiClientMock).searchConcepts(eq(conceptUUIDs));

        assertTrue(result.size() == 0);
    }

    @Test
    public void shouldSearchConcepts() throws JsonProcessingException {
        Concept concept = new Concept(UUID.randomUUID(), "somePrefLabel");
        Concept concept2 = new Concept(UUID.randomUUID(), "somePrefLabel2");
        Concept[] concepts = new Concept[] { concept, concept2 };
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(concepts);

        final Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(response.readEntity(eq(String.class))).thenReturn(message);

        String[] conceptUUIDs = new String[] { "123", "456" };
        when(publicConceptsApiClientMock.searchConcepts(eq(conceptUUIDs))).thenReturn(response);
        List<Concept> result = publicConceptApiService.searchConcepts(conceptUUIDs);
        verify(publicConceptsApiClientMock).searchConcepts(eq(conceptUUIDs));

        assertTrue(result.size() == 2);
    }

}
