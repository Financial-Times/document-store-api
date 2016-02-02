package com.ft.universalpublishing.documentstore.resources;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import io.dropwizard.testing.junit.ResourceTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.service.DocumentStoreService;
import com.ft.universalpublishing.documentstore.util.ContextBackedApiUriGeneratorProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;


public class DocumentQueryResourceEndpointTest {
    private final static DocumentStoreService DOC_STORE = mock(DocumentStoreService.class);
    private static final String API_URL_PREFIX = "localhost:12345";
    private static final String CONTENT = "content";
    private static final String AUTHORITY = "http://www.example.com/";
    private static final String IDENTIFIER_VALUE = "http://www.example.com/here-is-the-news";
    
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new DocumentQueryResource(DOC_STORE))
            .addProvider(new ContextBackedApiUriGeneratorProvider(API_URL_PREFIX))
            .build();

    private Client client;
    private ClientResponse response;
    
    @Before
    public void setup() {
        reset(DOC_STORE);
        
        client = resources.client();
        client.setFollowRedirects(false);
    }
    
    @After
    public void tearDown() {
        if (response != null) {
            response.close();
        }
    }
    
    @Test
    public void thatReturns301ForFoundDocument() {
        String uuid = UUID.randomUUID().toString();
        Map<String,Object> doc = Collections.singletonMap("uuid", uuid);
        when(DOC_STORE.findByIdentifier(CONTENT, AUTHORITY, IDENTIFIER_VALUE)).thenReturn(doc);
        
        response = client.resource("/content-query")
                         .queryParam("identifierAuthority", AUTHORITY)
                         .queryParam("identifierValue", IDENTIFIER_VALUE)
                         .get(ClientResponse.class);
        
        assertThat("response status", response.getStatus(), equalTo(301));
        assertThat("location", response.getHeaders().getFirst("Location"), equalTo("http://" + API_URL_PREFIX + "/content/" + uuid));
    }
    
    @Test
    public void thatReturns404ForNotFoundDocument() {
        when(DOC_STORE.findByIdentifier(eq(CONTENT), anyString(), anyString())).thenReturn(null);
        
        response = client.resource("/content-query")
                         .queryParam("identifierAuthority", AUTHORITY)
                         .queryParam("identifierValue", "http://www.example.com/no-such-item")
                         .get(ClientResponse.class);
        
        assertThat("response status", response.getStatus(), equalTo(404));
    }
    
    @Test(expected = QueryResultNotUniqueException.class)
    public void thatThrowsExceptionForNonUniqueResult() {
        when(DOC_STORE.findByIdentifier(eq(CONTENT), anyString(), anyString()))
            .thenThrow(new QueryResultNotUniqueException());
        
        response = client.resource("/content-query")
                         .queryParam("identifierAuthority", AUTHORITY)
                         .queryParam("identifierValue", IDENTIFIER_VALUE)
                         .get(ClientResponse.class);
    }
    
    @Test
    public void thatReturns400ForMissingQueryParameter() {
        response = client.resource("/content-query")
                         .queryParam("identifierValue", IDENTIFIER_VALUE)
                         .get(ClientResponse.class);
        
        assertThat("response status", response.getStatus(), equalTo(400));
    }
}
