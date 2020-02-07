package com.ft.universalpublishing.documentstore.service.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "filter")
                .withField("field test", "message test").build().logInfo();
        chain.doFilter(request, response);
    }
}
