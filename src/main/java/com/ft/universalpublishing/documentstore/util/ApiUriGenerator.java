package com.ft.universalpublishing.documentstore.util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

public interface ApiUriGenerator {


    /**
     * Gets the current rewritten URL.
     * @return a URL
     */
    String currentUri();

    /**
     * <p>Gets the current front-end host on it's own so that it can be used for your own URL transforms.</p>
     * <p>I'm not sure why you would ever want to call this method, given you have the source code for the whole class!</p>
     * @return a hostname
     */
    String getCurrentHost();

    /**
     * Gets the current front-end protocol on it's own so that it can be used for custom URL transforms
     * @return protocol
     */
    HttpProtocol getProtocol();

    /**
     * Removes parameters from the current URL and returns the rest of it
     * @see #currentUri()
     * @return a URI
     */
    String stripParameters();

    /**
     * Removes parameters from the current URL and returns the rest of it with new parameters.
     * @param queryString
     * @return a URI
     */
    String overrideParameters(MultivaluedMap<String, String> queryString);


    UriBuilder forResource(Class<?> aClass, String method);

    /**
     * Converts the relativePath to the appropriate absolute path for the users current route and protocol.
     * @param relativePath
     * @return an absolute URI.
     */
    String resolve(String relativePath);

}
