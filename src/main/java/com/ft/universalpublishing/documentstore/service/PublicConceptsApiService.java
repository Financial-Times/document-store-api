package com.ft.universalpublishing.documentstore.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ft.universalpublishing.documentstore.model.read.Concept;

/**
 * PublicConceptsApiService
 */
public interface PublicConceptsApiService {
    // the concept might be concorded with another one and if so, will be returned
    // by the public-content-api service
    public Concept getUpToDateConcept(Concept concept) throws JsonMappingException, JsonProcessingException;

    public List<Concept> searchConcepts(String[] conceptUUIDs) throws JsonMappingException, JsonProcessingException;
}
