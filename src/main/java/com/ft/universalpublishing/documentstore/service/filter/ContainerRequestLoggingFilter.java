package com.ft.universalpublishing.documentstore.service.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

public class ContainerRequestLoggingFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "filter").withRequest(request).build()
                .logInfo();

    }
}
