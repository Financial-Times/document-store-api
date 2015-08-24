package com.ft.universalpublishing.documentstore.util;

import com.ft.universalpublishing.documentstore.resources.DocumentResource;
import com.google.common.base.Optional;
import com.sun.jersey.api.uri.UriBuilderImpl;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.BeforeClass;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class ApiUriGeneratorTest {

    private static final String KITCHEN_SINK_URL = "http://example.com/path/file.cgi?foo=bar#top";

    @DataPoint
    public static ApiUriGenerator mockImplementation = new FixedUriGenerator(KITCHEN_SINK_URL);

    @DataPoint
    public static ApiUriGenerator realImplementation;

    @BeforeClass
    public static void setUpRealImplementation() {

        MultivaluedMap fooBar = new MultivaluedMapImpl();
        fooBar.put("foo", Collections.singletonList("bar"));

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePath()).thenReturn(URI.create(KITCHEN_SINK_URL));
        when(uriInfo.getQueryParameters(anyBoolean())).thenReturn(fooBar);
        when(uriInfo.getAbsolutePathBuilder()).thenAnswer(invocationOnMock -> {
            UriBuilder builder = new UriBuilderImpl();
            return applyDefaults(builder).path("/path/file.cgi").fragment("top");
        });
        when(uriInfo.getBaseUriBuilder()).thenAnswer(invocationOnMock -> applyDefaults(new UriBuilderImpl()));

        realImplementation = new ContextBackedUriGenerator(uriInfo, "example.com",HttpProtocol.HTTP, Optional.<Integer>absent());
    }

    private static UriBuilder applyDefaults(UriBuilder builder) {
        return builder.host("example.com")
                .scheme("http");
    }

    @Theory
    public void givenRequestedUrlWhenAskedShouldEchoTheSameUrl(ApiUriGenerator sit) {
        assertThat(sit.currentUri(),is(KITCHEN_SINK_URL));
    }

    @Theory
    public void givenRequestedUrlWhenParamsAreStrippedThenReturnRestOfUrl(ApiUriGenerator sit) {
        assertThat(sit.stripParameters(),is("http://example.com/path/file.cgi#top"));
    }

    @Theory
    public void givenRequestedUrlWhenParamsAreOverWrittenThenReturnUriWithOnlyNewParams(ApiUriGenerator sit) {

        MultivaluedMapImpl params = new MultivaluedMapImpl();
        params.put("baz", Collections.singletonList("spong"));

        assertThat(sit.overrideParameters(params),is("http://example.com/path/file.cgi?baz=spong#top"));
    }

    @Theory
    public void givenARequestUrlWhenCurrentHostRequestedShouldReturnTheHost(ApiUriGenerator sit) {
        assertThat(sit.getCurrentHost(), is("example.com"));
    }

    @Theory
    public void givenARequestUrlAndResourceShouldGenerateUriBuilderForResource(ApiUriGenerator sit) {
        assertThat(sit.forResource(DocumentResource.class, "getContentReadByUuid").build("foo").toString(),is("http://example.com/content-read/foo"));
    }

    @Theory
    public void shouldCorrectResolveRootRelativePath(ApiUriGenerator sit) {
        assertThat(sit.resolve("/second-resource"),is("http://example.com/second-resource"));
    }
}
