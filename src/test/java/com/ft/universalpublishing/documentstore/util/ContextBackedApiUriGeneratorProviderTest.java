package com.ft.universalpublishing.documentstore.util;

import com.ft.universalpublishing.documentstore.resources.DocumentResource;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContextBackedApiUriGeneratorProviderTest {

    public static final String EXAMPLE_GATEWAY_HOST = "int.api.ft.com";
    private static final String EXAMPLE_GATEWAY_BASE_URI = "http://" + EXAMPLE_GATEWAY_HOST;
    public static final String PATH = "/path";
    private static final String EXAMPLE_LOCAL_HOST = "localhost";
    public static final int LOCAL_PORT = 9090;
    private static final String EXAMPLE_LOCAL_HOST_AND_PORT = EXAMPLE_LOCAL_HOST + ":" + LOCAL_PORT;
    private static final String EXAMPLE_LOCAL_BASE_URI = "http://" + EXAMPLE_LOCAL_HOST_AND_PORT;


    @Mock
    HttpContext context;
    @Mock
    HttpRequestContext request;
    @Mock
    ExtendedUriInfo uriInfo;

    @Before
    public void setUpContextStructure() {
        when(context.getRequest()).thenReturn(request);
        when(context.getUriInfo()).thenReturn(uriInfo);

        // We aren't using parameters in this test, so just plumb in an empty map
        when(uriInfo.getQueryParameters(anyBoolean())).thenReturn(new MultivaluedMapImpl());
    }

    @Test
    public void shouldHandleGatewayHeadersAndSetUpGeneratorToCreateUrisWithNoPort() {
        filloutContext(EXAMPLE_LOCAL_HOST, EXAMPLE_LOCAL_BASE_URI);
        when(request.getHeaderValue(ContextBackedApiUriGeneratorProvider.HEADER_API_ROOT_PATH)).thenReturn(EXAMPLE_GATEWAY_HOST);
        when(request.getHeaderValue(ContextBackedApiUriGeneratorProvider.HEADER_FORWARDED_PROTO)).thenReturn("http");
        ContextBackedApiUriGeneratorProvider provider = new ContextBackedApiUriGeneratorProvider(EXAMPLE_LOCAL_HOST_AND_PORT);

        ApiUriGenerator generator = provider.getValue(context);
        assertThat(generator.currentUri(),is(EXAMPLE_GATEWAY_BASE_URI + PATH));
        assertThat(generator.forResource(DocumentResource.class, "getContentReadByUuid").build("theID").toString(),is(EXAMPLE_GATEWAY_BASE_URI + "/content-read/theID"));
    }

    @Test
    public void shouldHandleRequestWithNoHeaderAndSetUpGeneratorToCreateUrisWithMatchingPort() {
        filloutContext(EXAMPLE_LOCAL_HOST, EXAMPLE_LOCAL_BASE_URI);
        ContextBackedApiUriGeneratorProvider provider = new ContextBackedApiUriGeneratorProvider(EXAMPLE_LOCAL_HOST_AND_PORT);

        ApiUriGenerator generator = provider.getValue(context);

        assertThat(generator.currentUri(),is(EXAMPLE_LOCAL_BASE_URI + "/path"));
        assertThat(generator.forResource(DocumentResource.class, "getContentReadByUuid").build("theID").toString(),is(EXAMPLE_LOCAL_BASE_URI + "/content-read/theID"));
    }

    @Test
    public void shouldHandleHttpsRequestWithNoHeaderAndSetUpGeneratorToCreateUrisWithMatchingScheme() {

        String httpsLocalUrl = EXAMPLE_LOCAL_BASE_URI.replaceAll("http:", "https:");

        filloutContext(EXAMPLE_LOCAL_HOST,httpsLocalUrl);

        ContextBackedApiUriGeneratorProvider provider = new ContextBackedApiUriGeneratorProvider(EXAMPLE_LOCAL_HOST_AND_PORT);
        ApiUriGenerator generator = provider.getValue(context);

        assertThat(generator.currentUri(),is(httpsLocalUrl + "/path"));

    }

    @Test
    public void shouldFallBackToProxyHostAddressIfThatIsProvidedAndThereIsNoHeader() {

        filloutContext(EXAMPLE_LOCAL_HOST, EXAMPLE_LOCAL_BASE_URI);

        ContextBackedApiUriGeneratorProvider provider = new ContextBackedApiUriGeneratorProvider("varnish.example.com");
        ApiUriGenerator generator = provider.getValue(context);

        assertThat(generator.getCurrentHost(),is("varnish.example.com"));
    }

    @Test
    public void shouldFallBackToProxyHostAddressAndPortIfThatIsProvidedAndThereIsNoHeader() {

        filloutContext(EXAMPLE_LOCAL_HOST, EXAMPLE_LOCAL_BASE_URI);

        ContextBackedApiUriGeneratorProvider provider = new ContextBackedApiUriGeneratorProvider("varnish.example.com:8080");
        ApiUriGenerator generator = provider.getValue(context);

        assertThat(generator.getCurrentHost(),is("varnish.example.com"));
        assertThat(generator.currentUri(),containsString(":8080/"));
    }

    @Test
    public void shouldHandleHttpRequestWithNoHttpsHeaderAndSetUpGeneratorToCreateUrisWithHttps() {

        filloutContext(EXAMPLE_LOCAL_HOST,EXAMPLE_LOCAL_BASE_URI);

        when(request.getHeaderValue(ContextBackedApiUriGeneratorProvider.HEADER_API_ROOT_PATH)).thenReturn(EXAMPLE_GATEWAY_HOST);
        when(request.getHeaderValue(ContextBackedApiUriGeneratorProvider.HEADER_FORWARDED_PROTO)).thenReturn("https");


        ContextBackedApiUriGeneratorProvider provider = new ContextBackedApiUriGeneratorProvider(EXAMPLE_LOCAL_HOST_AND_PORT);
        ApiUriGenerator generator = provider.getValue(context);

        String httpsGatewayUrl = EXAMPLE_GATEWAY_BASE_URI.replaceAll("http:","https:");

        assertThat(generator.currentUri(),is(httpsGatewayUrl + "/path"));

    }

    private void filloutContext(String host, String baseUri) {
        URI gatewayUri = URI.create(baseUri);
        URI resourceUri = gatewayUri.resolve(PATH);

        when(request.getBaseUri()).thenReturn(gatewayUri);
        when(uriInfo.getBaseUri()).thenReturn(gatewayUri);
        when(uriInfo.getAbsolutePath()).thenReturn(resourceUri);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri(resourceUri));
        when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(baseUri));
    }
}
