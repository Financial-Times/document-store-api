package com.ft.universalpublishing.documentstore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DocumentStoreApiApplicationTest {

    @ClassRule
    public static final DropwizardAppRule RULE =
            new DropwizardAppRule<>(DocumentStoreApiApplication.class,
                    ResourceHelpers.resourceFilePath("config-no-mongo-test.yml"));

    @Test
    public void applicationShouldRegisterHealthChecksEvenIfMongoConnectionFails() throws IOException {
        Client client = new JerseyClientBuilder().build();

        Response response = client.target(
                String.format("http://localhost:%d/__health", RULE.getLocalPort()))
                .request()
                .get();


        final String payload = response.readEntity(String.class);
        final JsonNode jsonNode = new ObjectMapper().readValue(payload, JsonNode.class);

        // being sure that the node/field is valid/present in the response, default asBoolean call always returns false.
        final String ok = jsonNode.at("/checks/0/ok").asText();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("false", ok);
    }
}