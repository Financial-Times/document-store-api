package com.ft.universalpublishing.documentstore.handler;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ft.api.jaxrs.errors.ClientError;
import com.ft.universalpublishing.documentstore.model.read.Concordance;
import com.ft.universalpublishing.documentstore.model.read.Context;
import com.ft.universalpublishing.documentstore.service.PublicConcordancesApiService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetConcordedConceptsHandler implements Handler {

    PublicConcordancesApiService publicConcordancesApiService;

    @Override
    public void handle(final Context context) {
        final String conceptUUID = context.getConceptUUID();

        if (conceptUUID == null) {
            return;
        }

        try {
            final List<Concordance> uppConcordances = publicConcordancesApiService.getUPPConcordances(conceptUUID);
            UUID[] conceptUUIDs = uppConcordances.stream()
                    .map(concordance -> UUID.fromString(concordance.getIdentifier().getIdentifierValue()))
                    .toArray(UUID[]::new);

            if (conceptUUIDs.length == 0) {
                conceptUUIDs = new UUID[] { UUID.fromString(conceptUUID) };
            }

            context.addParameter("conceptUUIDs", conceptUUIDs);
        } catch (final JsonProcessingException e) {
            throw ClientError.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).error(e.getMessage()).exception();
        }
    }
}
