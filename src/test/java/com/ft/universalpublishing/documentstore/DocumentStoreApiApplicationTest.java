package com.ft.universalpublishing.documentstore;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class DocumentStoreApiApplicationTest {

    @ClassRule
    public static final DropwizardAppRule RULE =
            new DropwizardAppRule<>(DocumentStoreApiApplication.class,
                    ResourceHelpers.resourceFilePath("config-no-mongo-test.yml"));

    @Test
    public void applicationShouldRegisterHealthChecksEvenIfMongoConnectionFails() {
        Client client = new JerseyClientBuilder().build();

        Response response = client.target(
                String.format("http://localhost:%d/__health", RULE.getLocalPort()))
                .request()
                .get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}