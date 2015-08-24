package com.ft.universalpublishing.documentstore.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

public class ContextBackedUriGenerator implements ApiUriGenerator {

    private final Optional<Integer> port;
    private UriInfo pathInfo;
    private String frontendHost;
    private HttpProtocol protocol;

    public ContextBackedUriGenerator(UriInfo pathInfo, String frontendHost, HttpProtocol protocol, Optional<Integer> port) {
        Preconditions.checkArgument(pathInfo!=null,"pathInfo is missing");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(frontendHost),"frontendHost is missing or blank");
        Preconditions.checkArgument(protocol!=null,"protocol is missing");
        Preconditions.checkArgument(port!=null,"port must be populated or declared absent");
        this.pathInfo = pathInfo;
        this.frontendHost = frontendHost;
        this.protocol = protocol;
        this.port = port;
    }

    @Override
    public String currentUri() {
        return buildUrl(pathInfo.getAbsolutePathBuilder(), pathInfo.getQueryParameters(true));
    }

    @Override
    public String getCurrentHost() {
        return frontendHost;
    }

    @Override
    public HttpProtocol getProtocol() {
        return protocol;
    }

    @Override
    public String stripParameters() {
        return this.buildUrl(pathInfo.getAbsolutePathBuilder(), new MultivaluedMapImpl());
    }

    @Override
    public String overrideParameters(MultivaluedMap<String, String> queryString) {
        return buildUrl(pathInfo.getAbsolutePathBuilder(),queryString);
    }

    @Override
    public UriBuilder forResource(Class<?> aClass, String method) {
        return merge(pathInfo.getBaseUriBuilder().path(aClass, method));
    }

    private String buildUrl(UriBuilder builder, MultivaluedMap<String, String> queryString) {
        for(Map.Entry<String, List<String>> param : queryString.entrySet()) {
            builder.queryParam(param.getKey(),param.getValue().toArray());
        }
        return merge(builder)
                .build().toString();
    }

    @Override
    public String resolve(String rootRelativePath) {
        Preconditions.checkArgument(rootRelativePath.startsWith("/"), rootRelativePath + " does not start with a forward slash");
        return merge(UriBuilder.fromPath(rootRelativePath)).build().toString();
    }

    private UriBuilder merge(UriBuilder builder) {
        return builder
                .port(port.or(-1))
                .scheme(protocol.getValue())
                .host(frontendHost);
    }
}
