package com.ft.universalpublishing.documentstore.target;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
public class ApplyConcordedConceptsToListsTarget implements Target {

    PublicConceptsApiService publicConceptsApiService;

    @Override
    public Object execute(final Context context) {
        final List<Document> listDocuments = context.getDocuments();

        // get all concepts from the lists and deduplicate them
        Set<String> strings = listDocuments.stream().map(list -> {
            final Document concept = (Document) list.get("concept");
            return (String) concept.get("uuid");
        }).collect(Collectors.toSet());
        String[] conceptUUIDs = new String[] {};
        conceptUUIDs = strings.toArray(conceptUUIDs);

        try {
            final List<Concept> conceptsFound = publicConceptsApiService.searchConcepts(conceptUUIDs);
            final Map<String, Concept> originalUUIDForConcepts = new HashMap<>();
            conceptsFound.forEach(concept -> originalUUIDForConcepts.put(concept.getOriginalUUID(), concept));

            listDocuments.forEach(list -> {
                final Document conceptDocument = (Document) list.get("concept");
                final String originalUUID = (String) conceptDocument.get("uuid");
                final Concept concept = originalUUIDForConcepts.get(originalUUID);
                conceptDocument.put("uuid", concept.getUuid().toString());
                conceptDocument.put("prefLabel", concept.getPrefLabel());
            });

            return listDocuments;

        } catch (final JsonProcessingException e) {
            throw ClientError.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
    }
}
