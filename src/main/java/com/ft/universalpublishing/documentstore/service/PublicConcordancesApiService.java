package com.ft.universalpublishing.documentstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ft.universalpublishing.documentstore.model.read.Concordance;
import java.util.List;

public interface PublicConcordancesApiService {

  List<Concordance> getUPPConcordances(String conceptUUID)
      throws JsonMappingException, JsonProcessingException;
}
