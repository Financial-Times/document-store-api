package com.ft.universalpublishing.documentstore.service.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;

import com.ft.universalpublishing.documentstore.utils.FluentLoggingBuilder;

public class ContainerResponseLoggingFilter implements ContainerResponseFilter {

    @Context
    private HttpServletResponse httpServletResponse;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        FluentLoggingBuilder.getNewInstance(this.getClass().getCanonicalName(), "filter")
                .withField("field test", "message test").build().logInfo();
    }
}
