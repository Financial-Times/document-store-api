package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.universalpublishing.documentstore.handler.ConceptUuidValidationHandler;
import com.ft.universalpublishing.documentstore.handler.FilterListsHandler;
import com.ft.universalpublishing.documentstore.handler.GetConcordedConceptsHandler;
import com.ft.universalpublishing.documentstore.handler.Handler;
import com.ft.universalpublishing.documentstore.handler.HandlerChain;
import com.ft.universalpublishing.documentstore.model.read.Concept;
import com.ft.universalpublishing.documentstore.model.read.Concordance;
import com.ft.universalpublishing.documentstore.model.read.ContentList;
import com.ft.universalpublishing.documentstore.model.read.Identifier;
import com.ft.universalpublishing.documentstore.model.read.ListItem;
import com.ft.universalpublishing.documentstore.model.read.Operation;
import com.ft.universalpublishing.documentstore.model.read.Pair;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import com.ft.universalpublishing.documentstore.service.PublicConceptsApiService;
import com.ft.universalpublishing.documentstore.service.PublicConcordancesApiService;
import com.ft.universalpublishing.documentstore.target.ApplyConcordedConceptsToListsTarget;
import com.ft.universalpublishing.documentstore.target.Target;
import com.ft.universalpublishing.documentstore.validators.ContentListValidator;
import com.ft.universalpublishing.documentstore.validators.UuidValidator;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DocumentSearchListResourceEndpointTest {
    private final static MongoDocumentStoreService documentStoreService = mock(MongoDocumentStoreService.class);
    private final static PublicConceptsApiService publicConceptsApiService = mock(PublicConceptsApiService.class);
    private final static PublicConcordancesApiService publicConcordancesApiService = mock(
            PublicConcordancesApiService.class);
    private final static ContentListValidator contentListValidator = mock(ContentListValidator.class);
    private static final String RESOURCE_TYPE = "lists";
    private static final String CONCEPT_PREF_LABEL = "World";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ResourceExtension resources = ResourceExtension.builder()
            .addResource(new DocumentResource(getCollectionMap())).build();

    public DocumentSearchListResourceEndpointTest() {
    }

    @BeforeEach
    public void setup() {
        reset(documentStoreService);
        reset(publicConceptsApiService);
        reset(publicConcordancesApiService);
        reset(contentListValidator);
    }

    @Test
    public void shouldReturn200ForEmptySearch() throws JsonMappingException, JsonProcessingException {
        final List<ContentList> contentLists = createContentList(3);
        final List<Document> documents = convertToDocuments(contentLists);
        final List<Concept> searchConceptsResults = createClonedConceptsListFromContentList(contentLists);
        searchConceptsResults.forEach(concept -> concept.setOriginalUUID(concept.getUuid().toString()));
        final Set<String> searchConceptsUUIDs = searchConceptsResults.stream()
                .map(concept -> concept.getUuid().toString()).collect(Collectors.toSet());

        when(documentStoreService.filterLists(eq(RESOURCE_TYPE), eq(null), eq(null), eq(null))).thenReturn(documents);
        when(publicConceptsApiService.searchConcepts(any(String[].class))).thenReturn(searchConceptsResults);

        final Response clientResponse = resources.client().target("/search/lists").request().get();

        final ArgumentCaptor<String[]> conceptsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(publicConcordancesApiService, times(0)).getUPPConcordances(anyString());
        verify(documentStoreService).filterLists(eq(RESOURCE_TYPE), eq(null), eq(null), eq(null));
        verify(publicConceptsApiService).searchConcepts(conceptsCaptor.capture());

        final List<String> searchConceptsParam = Arrays.asList(conceptsCaptor.getValue());
        searchConceptsParam.forEach(searchConceptParam -> assertTrue(searchConceptsUUIDs.contains(searchConceptParam)));
        assertThat(searchConceptsParam.size(), equalTo(searchConceptsResults.size()));

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final List<ContentList> retrievedLists = Arrays.asList(clientResponse.readEntity(ContentList[].class));
        retrievedLists
                .forEach(list -> assertTrue(searchConceptsUUIDs.contains(list.getConcept().getUuid().toString())));

        assertThat(retrievedLists.size(), equalTo(contentLists.size()));
    }

    @Test
    public void shouldReturn200ForGivenConceptNoConcordance() throws JsonMappingException, JsonProcessingException {
        final List<ContentList> contentLists = createContentList(1);
        final int listIndex = 0;
        final List<Document> documents = convertToDocuments(Arrays.asList(contentLists.get(listIndex)));
        final List<Concept> searchConceptsResults = createClonedConceptsListFromContentList(contentLists);
        searchConceptsResults.forEach(concept -> concept.setOriginalUUID(concept.getUuid().toString()));
        final Set<String> searchConceptsUUIDs = searchConceptsResults.stream()
                .map(concept -> concept.getUuid().toString()).collect(Collectors.toSet());

        final UUID[] conceptParams = new UUID[] { contentLists.get(listIndex).getConcept().getUuid() };

        when(documentStoreService.filterLists(eq(RESOURCE_TYPE), eq(conceptParams), eq(null), eq(null)))
                .thenReturn(documents);
        when(publicConceptsApiService.searchConcepts(any(String[].class))).thenReturn(searchConceptsResults);

        final Response clientResponse = resources.client().target("/search/lists")
                .queryParam("conceptUUID", conceptParams[0].toString()).request().get();

        final ArgumentCaptor<String[]> conceptsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(publicConcordancesApiService).getUPPConcordances(eq(conceptParams[0].toString()));
        verify(documentStoreService).filterLists(eq(RESOURCE_TYPE), eq(conceptParams), eq(null), eq(null));
        verify(publicConceptsApiService).searchConcepts(conceptsCaptor.capture());

        final List<String> searchConceptsParam = Arrays.asList(conceptsCaptor.getValue());
        searchConceptsParam.forEach(searchConceptParam -> assertTrue(searchConceptsUUIDs.contains(searchConceptParam)));
        assertThat(searchConceptsParam.size(), equalTo(searchConceptsResults.size()));

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final List<ContentList> retrievedLists = Arrays.asList(clientResponse.readEntity(ContentList[].class));
        retrievedLists.forEach(list -> {
            assertTrue(searchConceptsUUIDs.contains(list.getConcept().getUuid().toString()));
        });

        assertThat(retrievedLists.size(), equalTo(1));
    }

    @Test
    public void shouldReturn200ForGivenConceptWithConcordancesAndListTypeAndSearchTerm()
            throws JsonMappingException, JsonProcessingException {
        final List<ContentList> contentLists = createContentList(2);
        final List<Document> documents = convertToDocuments(contentLists);
        final Concept concordedConcept = new Concept(UUID.randomUUID(), "somePrefLabel");
        final Concept concordedConcept2 = new Concept(UUID.randomUUID(), "somePrefLabel2");
        final String conceptUUID = concordedConcept.getUuid().toString();
        final Identifier identifier = new Identifier("http://api.ft.com/system/UPP", UUID.randomUUID().toString());
        final Identifier identifier2 = new Identifier("http://api.ft.com/system/UPP", UUID.randomUUID().toString());
        final List<Concept> searchConceptsResults = createClonedConceptsListFromContentList(contentLists);
        final Set<String> searchConceptsUUIDs = contentLists.stream().map(c -> c.getConcept().getUuid().toString())
                .collect(Collectors.toSet());
        final Concordance concordance = new Concordance(concordedConcept, identifier);
        final Concordance concordance2 = new Concordance(concordedConcept2, identifier2);
        final List<Concordance> concordances = Arrays.asList(concordance, concordance2);

        final UUID[] conceptParams = concordances.stream()
                .map(c -> UUID.fromString(c.getIdentifier().getIdentifierValue())).toArray(UUID[]::new);

        final String listType = "TopStories";
        final String title = "New Title";

        when(publicConcordancesApiService.getUPPConcordances(eq(conceptUUID))).thenReturn(concordances);
        when(documentStoreService.filterLists(eq(RESOURCE_TYPE), eq(conceptParams), eq(listType), eq(title)))
                .thenReturn(documents);
        when(publicConceptsApiService.searchConcepts(any(String[].class))).thenReturn(searchConceptsResults);

        final Response clientResponse = resources.client().target("/search/lists")
                .queryParam("conceptUUID", conceptUUID).queryParam("listType", listType).queryParam("searchTerm", title)
                .request().get();

        final ArgumentCaptor<String[]> conceptsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(publicConcordancesApiService).getUPPConcordances(eq(conceptUUID));
        verify(documentStoreService).filterLists(eq(RESOURCE_TYPE), eq(conceptParams), eq(listType), eq(title));
        verify(publicConceptsApiService).searchConcepts(conceptsCaptor.capture());

        final List<String> searchConceptsParam = Arrays.asList(conceptsCaptor.getValue());
        searchConceptsParam.forEach(searchConceptParam -> assertTrue(searchConceptsUUIDs.contains(searchConceptParam)));
        assertThat(searchConceptsParam.size(), equalTo(searchConceptsResults.size()));

        assertThat("response", clientResponse, hasProperty("status", equalTo(200)));
        final List<ContentList> retrievedLists = Arrays.asList(clientResponse.readEntity(ContentList[].class));
        retrievedLists.forEach(list -> {
            assertTrue(searchConceptsUUIDs.contains(list.getConcept().getUuid().toString()));
        });

        assertThat(retrievedLists.size(), equalTo(2));
    }

    @Test
    public void shouldReturn400ForInvalidConceptUUID() throws JsonMappingException, JsonProcessingException {
        final String conceptUUID = "invalid-uuid";

        final Response clientResponse = resources.client().target("/search/lists")
                .queryParam("conceptUUID", conceptUUID).request().get();

        assertThat("response", clientResponse, hasProperty("status", equalTo(400)));
    }

    private static Map<Pair<String, Operation>, HandlerChain> getCollectionMap() {
        final Handler conceptUuidValidationHandler = new ConceptUuidValidationHandler(new UuidValidator());
        final Handler getConcordedConceptsHandler = new GetConcordedConceptsHandler(publicConcordancesApiService);
        final Handler getSearchResultsHandler = new FilterListsHandler(documentStoreService);
        final Target applyConcordedConceptsToLists = new ApplyConcordedConceptsToListsTarget(publicConceptsApiService);

        final Map<Pair<String, Operation>, HandlerChain> collections = new HashMap<>();
        collections.put(new Pair<>("lists", Operation.SEARCH),
                new HandlerChain()
                        .addHandlers(conceptUuidValidationHandler, getConcordedConceptsHandler, getSearchResultsHandler)
                        .setTarget(applyConcordedConceptsToLists));

        return collections;
    }

    private static ContentList createContentList(final String listUuid, final String firstContentUuid,
            final String secondContentUuid, final boolean addConcept) {
        final ListItem contentItem1 = new ListItem();
        contentItem1.setUuid(firstContentUuid);
        final ListItem contentItem2 = new ListItem();
        contentItem2.setUuid(secondContentUuid);
        final List<ListItem> content = ImmutableList.of(contentItem1, contentItem2);

        final ContentList.Builder builder = new ContentList.Builder().withUuid(UUID.fromString(listUuid))
                .withItems(content).withTitle(RandomStringUtils.randomAlphabetic(10))
                .withListType(RandomStringUtils.randomAlphabetic(10));

        if (addConcept) {
            final Concept concept = new Concept(UUID.randomUUID(), CONCEPT_PREF_LABEL);
            final URI id = URI.create(String.format("http://api.ft.com/things/%s", concept.getUuid()));
            concept.setId(id);
            builder.withConcept(concept);
        }

        return builder.build();
    }

    private List<ContentList> createContentList(final int count) {
        final List<ContentList> contentList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            contentList.add(createContentList(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(), true));
        }
        return contentList;
    }

    private List<Document> convertToDocuments(final List<ContentList> contentLists) {
        return contentLists.stream().map(contentList -> {
            final Document document = new Document(objectMapper.convertValue(contentList, Map.class));
            document.put("concept", new Document(objectMapper.convertValue(contentList.getConcept(), Map.class)));
            return document;
        }).collect(Collectors.toList());
    }

    private List<Concept> createClonedConceptsListFromContentList(final List<ContentList> contentLists)
            throws JsonProcessingException {
        return contentLists.stream()
                .map(contentList -> assertDoesNotThrow(() -> objectMapper
                        .readValue(objectMapper.writeValueAsString(contentList.getConcept()), Concept.class)))
                .collect(Collectors.toList());
    }
}
