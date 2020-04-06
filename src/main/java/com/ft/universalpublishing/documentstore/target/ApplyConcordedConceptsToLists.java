package com.ft.universalpublishing.documentstore.target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiService;

import org.bson.Document;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplyConcordedConceptsToLists implements Target {

    PublicConceptsApiService publicConceptsApiService;
    String apiPath;

    @Override
    public Object execute(Context context) {
        // try {
        List<Document> listDocuments = (List<Document>) context.getParameter("listDocuments");
        String[] conceptUUIDs = listDocuments.stream().map(list -> {
            Document concept = (Document) list.get("concept");
            return (String) concept.get("uuid");
        }).toArray(String[]::new);
        try {
            List<Concept> conceptsFound = publicConceptsApiService.searchConcepts(conceptUUIDs);
            Map<String, Concept> originalUUIDForConcepts = new HashMap<>();
            conceptsFound.forEach(concept -> originalUUIDForConcepts.put(concept.getOriginalUUID(), concept));

            listDocuments.forEach(list -> {
                Document conceptDocument = (Document) list.get("concept");
                String originalUUID = (String) conceptDocument.get("uuid");
                Concept concept = originalUUIDForConcepts.get(originalUUID);
                conceptDocument.put("uuid", concept.getUuid().toString());
                conceptDocument.put("prefLabel", concept.getPrefLabel());

            });
            // .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            ClientError.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
        // concordedConcept.setUuid(null);

        // contentList.setConcept(concordedConcept);
        return listDocuments;
        // } catch (IllegalArgumentException | JsonProcessingException e) {
        // throw
        //
        // ClientError.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        // }
    }
}
