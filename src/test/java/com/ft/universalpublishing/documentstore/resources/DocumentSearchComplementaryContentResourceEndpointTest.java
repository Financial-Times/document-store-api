package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.target.FilterContentTarget;
import com.ft.universalpublishing.documentstore.target.Target;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentSearchComplementaryContentResourceEndpointTest {
  private static final MongoDocumentStoreService documentStoreService =
      mock(MongoDocumentStoreService.class);
  private static final ContentListValidator contentListValidator = mock(ContentListValidator.class);
  private static final String COLLECTION_NAME = "complementarycontent";

  private static final ResourceExtension resources =
      ResourceExtension.builder().addResource(new DocumentResource(getCollectionMap())).build();
  private static String CONTENT_PLACEHOLDER_MOCK_STRING = null;

  public DocumentSearchComplementaryContentResourceEndpointTest() {}

  @BeforeEach
  public void setup() {
    reset(documentStoreService);
    reset(contentListValidator);
  }

  @Test
  public void shouldReturn200ForEmptySearch() throws JsonMappingException, JsonProcessingException {
    final List<Document> dataDocuments = createContentPlaceholderDocumentsList(3);

    when(documentStoreService.filterCollection(
            eq(COLLECTION_NAME), eq(null), eq(null), eq(null), eq(null), eq(null)))
        .thenReturn(dataDocuments);

    final Response clientResponse =
        resources.client().target("/search/complementarycontent").request().get();

    verify(documentStoreService)
        .filterCollection(eq(COLLECTION_NAME), eq(null), eq(null), eq(null), eq(null), eq(null));

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    final List<ContentList> retrievedDocuments =
        Arrays.asList(clientResponse.readEntity(ContentList[].class));

    assertThat(retrievedDocuments.size(), equalTo(dataDocuments.size()));
  }

  @Test
  public void shouldReturn200ForGivenSearchTermWebUrlAndStandfirst()
      throws JsonMappingException, JsonProcessingException {
    final String title = "New Title";
    final String webUrl = "http://www.ft.com/ig/sites/2014/virgingroup-timeline-changed/";
    final String standfirst = "New Standfirst";

    final List<Document> dataDocuments = createContentPlaceholderDocumentsList(3);
    dataDocuments.get(0).put("title", title);
    dataDocuments.get(0).put("webUrl", webUrl);
    dataDocuments.get(0).put("standfirst", standfirst);

    when(documentStoreService.filterCollection(
            eq(COLLECTION_NAME), eq(null), eq(null), eq(title), eq(webUrl), eq(standfirst)))
        .thenReturn(dataDocuments);

    final Response clientResponse =
        resources
            .client()
            .target("/search/complementarycontent")
            .queryParam("webUrl", webUrl)
            .queryParam("searchTerm", title)
            .queryParam("standfirst", standfirst)
            .request()
            .get();

    verify(documentStoreService)
        .filterCollection(
            eq(COLLECTION_NAME), eq(null), eq(null), eq(title), eq(webUrl), eq(standfirst));

    assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
    final List<Document> retrievedDocuments =
        Arrays.asList(clientResponse.readEntity(Document[].class));

    assertThat(retrievedDocuments.size(), equalTo(dataDocuments.size()));
    assertThat(retrievedDocuments, equalTo(dataDocuments));
  }

  private static Map<Pair<String, Operation>, HandlerChain> getCollectionMap() {
    Target filterContentTarget = new FilterContentTarget(documentStoreService);

    final Map<Pair<String, Operation>, HandlerChain> collections = new HashMap<>();

    collections.put(
        new Pair<>("complementarycontent", Operation.SEARCH),
        new HandlerChain().setTarget(filterContentTarget));

    return collections;
  }

  private void readCPHMock() {
    URL url = Resources.getResource("cph-mock.json");
    try {
      CONTENT_PLACEHOLDER_MOCK_STRING = Resources.toString(url, Charsets.UTF_8);
    } catch (IOException e) {
      fail("Error parsing chp-mock.json file");
    }
  }

  private Document createContentPlaceholderDocument() {
    if (Strings.isNullOrEmpty(CONTENT_PLACEHOLDER_MOCK_STRING)) {
      readCPHMock();
    }
    Document contentPlaceholderDocument = Document.parse(CONTENT_PLACEHOLDER_MOCK_STRING);
    contentPlaceholderDocument.put("uuid", UUID.randomUUID().toString());
    return contentPlaceholderDocument;
  }

  private List<Document> createContentPlaceholderDocumentsList(int count) {
    final List<Document> dataDocuments = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      dataDocuments.add(createContentPlaceholderDocument());
    }

    return dataDocuments;
  }
}
