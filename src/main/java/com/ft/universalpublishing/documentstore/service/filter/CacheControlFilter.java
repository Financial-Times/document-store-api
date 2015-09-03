package com.ft.universalpublishing.documentstore.service.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheControlFilter implements Filter{

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Cache-Control", "max-age=30");
        resp.setHeader("Vary", "Accept");
        chain.doFilter(request, response);
    }

    public void destroy() {}

    public void init(FilterConfig arg0) throws ServletException {}
}
