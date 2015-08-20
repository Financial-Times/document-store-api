package com.ft.universalpublishing.documentstore.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.sun.jersey.api.core.HttpContext;

import javax.ws.rs.ext.Provider;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.apache.commons.lang.StringUtils.trimToNull;

@Provider
public class ContextBackedApiUriGeneratorProvider extends AbstractInjectableProvider<ApiUriGenerator> {

    public static final String HEADER_API_ROOT_PATH = "X-API-Root-Path";
    public static final String HEADER_FORWARDED_PROTO = "X-Forwarded-Proto";
    private HostAndPort httpCacheHost;

    public ContextBackedApiUriGeneratorProvider(String httpCacheHostAndPort) {
        super(ApiUriGenerator.class);
        this.httpCacheHost = HostAndPort.fromString(httpCacheHostAndPort);
    }

    @Override
    public ApiUriGenerator getValue(HttpContext c) {
        String host = readAccurateFrontEndHost(c);
        HttpProtocol protocol = HttpProtocol.fromString(seekAccurateFrontEndProtocol(c, "http"));
        Optional<Integer> port = Optional.of(httpCacheHost.getPortOrDefault(-1)); // -1 is converted to absent on next line
        if(hasExternalProtocol(c) || port.get() < 0) {
           port = Optional.absent();
        }
        return new ContextBackedUriGenerator(c.getUriInfo(), host , protocol, port);
    }

    private boolean hasExternalProtocol(HttpContext c) {
        return !Strings.isNullOrEmpty(c.getRequest().getHeaderValue(HEADER_FORWARDED_PROTO));
    }

    private String seekAccurateFrontEndProtocol(HttpContext c, String defaultProtocol) {
        String standardProtocol = trimToNull(c.getUriInfo().getAbsolutePath().getScheme());
        String headerProtocol = trimToNull(c.getRequest().getHeaderValue(HEADER_FORWARDED_PROTO));
        String fallbackProtocol = firstNonNull(standardProtocol, defaultProtocol);
        return firstNonNull(headerProtocol, fallbackProtocol);
    }

    private String readAccurateFrontEndHost(HttpContext c) {
        String standardHost = trimToNull(c.getUriInfo().getBaseUri().getHost());
        String headerHost = trimToNull(c.getRequest().getHeaderValue(HEADER_API_ROOT_PATH));
        return firstNonNull(headerHost, firstNonNull(httpCacheHost.getHostText(), standardHost));
    }
}
