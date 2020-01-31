package com.ft.universalpublishing.documentstore.service.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CacheControlFilterTest {
    private CacheControlFilter filter;
    @Mock
    private FilterChain chain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        filter = new CacheControlFilter("max-age=10");
    }

    @Test
    public void testHeaderContainsCacheMaxAge() throws IOException, ServletException {
        filter.doFilter(request, response, chain);
        verify(response).setHeader("Cache-Control", "max-age=10");
        verify(response).setHeader("Vary", "Accept");
        verify(chain).doFilter(request, response);
    }
}
