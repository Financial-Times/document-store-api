package com.ft.universalpublishing.documentstore.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ft.universalpublishing.documentstore.model.read.Concordance;

/**
 * PublicConceptsApiService
 */
public interface PublicConcordancesApiService {

    List<Concordance> getUPPConcordances(String conceptUUID) throws JsonMappingException, JsonProcessingException;
}
