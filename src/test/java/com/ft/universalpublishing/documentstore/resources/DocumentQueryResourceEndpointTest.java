package com.ft.universalpublishing.documentstore.resources;

import com.ft.universalpublishing.documentstore.exception.QueryResultNotUniqueException;
import com.ft.universalpublishing.documentstore.service.MongoDocumentStoreService;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


public class DocumentQueryResourceEndpointTest {
    private final static MongoDocumentStoreService DOC_STORE = mock(MongoDocumentStoreService.class);
    private static final String API_URL_PREFIX = "localhost:12345";
    private static final String CONTENT = "content";
    private static final String AUTHORITY = "http://www.example.com/";
    private static final String IDENTIFIER_VALUE = "http://www.example.com/here-is-the-news";
    
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new DocumentQueryResource(DOC_STORE, API_URL_PREFIX))
            .build();

    private Client client;
    private Response response;
    
    @Before
    public void setup() {
        reset(DOC_STORE);
        
        client = resources.client();
        //client.setFollowRedirects(false);
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
        
        response = client.target("/content-query")
                         .queryParam("identifierAuthority", AUTHORITY)
                         .queryParam("identifierValue", IDENTIFIER_VALUE)
                         .request()
                         .get(Response.class);
        
        assertThat("response status", response.getStatus(), equalTo(301));
        assertThat("location", (String) response.getHeaders().getFirst("Location"), equalTo("http://" + API_URL_PREFIX + "/content/" + uuid));
    }
    
    @Test
    public void thatReturns404ForNotFoundDocument() {
        when(DOC_STORE.findByIdentifier(eq(CONTENT), anyString(), anyString())).thenReturn(null);
        
        response = client.target("/content-query")
                         .queryParam("identifierAuthority", AUTHORITY)
                         .queryParam("identifierValue", "http://www.example.com/no-such-item")
                         .request()
                         .get(Response.class);
        
        assertThat("response status", response.getStatus(), equalTo(404));
    }
    
    @Test
    public void thatThrowsExceptionForNonUniqueResult() {
        when(DOC_STORE.findByIdentifier(eq(CONTENT), anyString(), anyString()))
            .thenThrow(new QueryResultNotUniqueException());
        
        response = client.target("/content-query")
                         .queryParam("identifierAuthority", AUTHORITY)
                         .queryParam("identifierValue", IDENTIFIER_VALUE)
                         .request()
                         .get(Response.class);
        
        assertThat("response status", response.getStatus(), equalTo(500));
    }
    
    @Test
    public void thatReturns400ForMissingQueryParameter() {
        response = client.target("/content-query")
                         .queryParam("identifierValue", IDENTIFIER_VALUE)
                         .request()
                         .get(Response.class);
        
        assertThat("response status", response.getStatus(), equalTo(400));
    }
}
