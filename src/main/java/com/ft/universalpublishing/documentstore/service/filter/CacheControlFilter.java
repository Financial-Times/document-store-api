package com.ft.universalpublishing.documentstore.service.filter;

import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class CacheControlFilter implements Filter {

    private final String cacheControl;

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Cache-Control", cacheControl);
        resp.setHeader("Vary", "Accept");
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    public void init(FilterConfig arg0) {
    }
}
