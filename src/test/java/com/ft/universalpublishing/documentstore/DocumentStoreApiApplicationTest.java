package com.ft.universalpublishing.documentstore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentStoreApiApplicationTest {

  private static final DropwizardAppExtension<DocumentStoreApiConfiguration> DW =
      new DropwizardAppExtension<>(
          DocumentStoreApiApplication.class,
          ResourceHelpers.resourceFilePath("config-no-mongo-test.yml"));

  @Test
  public void shouldRegisterApplicationHealthChecksEvenIfMongoConnectionFails() throws IOException {
    Client client = new JerseyClientBuilder().build();

    Response response =
        client
            .target(String.format("http://localhost:%d/__health", DW.getLocalPort()))
            .request()
            .get();

    final String payload = response.readEntity(String.class);
    final JsonNode jsonNode = new ObjectMapper().readValue(payload, JsonNode.class);

    // being sure that the node/field is valid/present in the response, default asBoolean call
    // always returns false.
    final String ok = jsonNode.at("/checks/0/ok").asText();

    assertThat(Response.Status.OK.getStatusCode(), equalTo(response.getStatus()));
    assertThat("false", equalTo(ok));
  }
}
