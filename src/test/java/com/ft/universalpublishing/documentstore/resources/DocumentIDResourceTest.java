package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import com.ft.universalpublishing.documentstore.exception.IDStreamingException;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentIDResourceTest {
  private static final MongoDocumentStoreService documentStoreService =
      mock(MongoDocumentStoreService.class);
  private static final String RESOURCE_TYPE = "content";
  private String IDS_PATH = "/" + RESOURCE_TYPE + "/" + "__ids";

  private static final ResourceExtension resources =
      ResourceExtension.builder().addResource(new DocumentIDResource(documentStoreService)).build();

  @Test
  public void shouldReturn200WhenIDsSuccessfully() throws IOException {
    final String firstUUID = "d08ef814-f295-11e6-a94b-0e7d0412f5a5";
    final String secondUUID = "8ae3f1dc-f288-11e6-8758-6876151821a6";
    doAnswer(
            invocationOnMock -> {
              Object[] args = invocationOnMock.getArguments();
              OutputStream outputStream1 = (OutputStream) args[2];
              outputStream1.write((new Document("uuid", firstUUID).toJson() + "\n").getBytes());
              outputStream1.write((new Document("uuid", secondUUID).toJson() + "\n").getBytes());
              return null;
            })
        .when(documentStoreService)
        .findUUIDs(eq(RESOURCE_TYPE), eq(Boolean.FALSE), any(OutputStream.class));
    Response clientResponse = resources.client().target(IDS_PATH).request().get();

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    StringWriter writer = new StringWriter();
    IOUtils.copy((InputStream) clientResponse.getEntity(), writer, "UTF-8");
    assertTrue(writer.toString().contains("{\"uuid\": \"" + firstUUID + "\"}"));
    assertTrue(writer.toString().contains("{\"uuid\": \"" + secondUUID + "\"}"));
  }

  @Test
  public void shouldReturn500WhenGettingIdsFails() throws IOException {
    doThrow(new IDStreamingException(RESOURCE_TYPE))
        .when(documentStoreService)
        .findUUIDs(eq(RESOURCE_TYPE), eq(Boolean.FALSE), any(OutputStream.class));
    Response clientResponse = resources.client().target(IDS_PATH).request().get();
    assertThat("response", clientResponse, hasProperty("status", equalTo(500)));
  }
}
