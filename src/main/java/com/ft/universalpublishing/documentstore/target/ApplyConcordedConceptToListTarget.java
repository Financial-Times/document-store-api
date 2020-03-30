package com.ft.universalpublishing.documentstore.target;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplyConcordedConceptToListTarget implements Target {

    PublicConceptsApiService publicConceptsApiService;
    String apiPath;

    @Override
    public Object execute(Context context) {
        try {
            ContentList contentList = new ObjectMapper().convertValue(context.getContentMap(), ContentList.class);
            Concept concept = contentList.getConcept();
            String conceptUUID = null;

            if (concept != null) {
                conceptUUID = concept.extractConceptUuid();
            }

            Concept concordedConcept = publicConceptsApiService.getUpToDateConcept(conceptUUID);
            contentList.setConcept(concordedConcept);
            contentList.addIds();
            contentList.addApiUrls(apiPath);
            contentList.removePrivateFields();

            return contentList;
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw ClientError.status(SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
    }
}
