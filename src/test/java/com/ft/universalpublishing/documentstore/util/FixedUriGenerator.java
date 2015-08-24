package com.ft.universalpublishing.documentstore.util;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class FixedUriGenerator implements ApiUriGenerator {

    public static ApiUriGenerator localUriGenerator(){
        return new FixedUriGenerator("http://localhost/");
    }

    private URI fixedRequestUri;

    public FixedUriGenerator(String fixedUri) {
        this(URI.create(fixedUri));
    }

    public FixedUriGenerator(URI fixedUri) {
        this.fixedRequestUri = fixedUri;
    }

    @Override
    public String currentUri() {
        return fixedRequestUri.toString();
    }

    @Override
    public String getCurrentHost() {
        return currentHostAndPort().getHostText();
    }

    @Override
    public HttpProtocol getProtocol() {
        return null;
    }

    @Override
    public String stripParameters() {
        return cloneWithoutParameters()
                .build().toString();
    }

    private UriBuilder cloneWithoutParameters() {
        return merge(UriBuilder.fromPath(fixedRequestUri.getPath())).fragment(fixedRequestUri.getFragment());
    }

    private UriBuilder merge(UriBuilder builder) {
        HostAndPort authority = currentHostAndPort();
        builder.host(authority.getHostText())
            .scheme(fixedRequestUri.getScheme());
        if(authority.getPort()!=80 || fixedRequestUri.getAuthority().contains(":80")) {
            builder.port(authority.getPort());
        }
        return builder;
    }

    private HostAndPort currentHostAndPort() {
        HostAndPort authority;
        if(fixedRequestUri.getAuthority().contains(":")) {
            authority = HostAndPort.fromString(fixedRequestUri.getAuthority());
        } else {
            authority = HostAndPort.fromParts(fixedRequestUri.getAuthority(), 80);
        }
        return authority;
    }

    @Override
    public String overrideParameters(MultivaluedMap<String, String> queryString) {
        UriBuilder builder = cloneWithoutParameters();
        for(String param : queryString.keySet()) {
            for(String value : queryString.get(param)) {
               builder.queryParam(param,value);
            }
        }
        return builder.build().toString();
    }

    @Override
    public UriBuilder forResource(Class<?> aClass, String method) {
        try {
            return merge(UriBuilder.fromResource(aClass).path(aClass.getMethod(method, String.class, ApiUriGenerator.class)));
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String resolve(String rootRelativePath) {
        Preconditions.checkArgument(rootRelativePath.startsWith("/"));
        return merge(UriBuilder.fromPath(rootRelativePath)).build().toString();
    }
}
