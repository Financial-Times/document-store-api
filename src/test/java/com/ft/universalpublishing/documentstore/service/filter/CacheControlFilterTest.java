package com.ft.universalpublishing.documentstore.service.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CacheControlFilterTest {

    private CacheControlFilter filter;

    @Mock
    FilterChain chain;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    @Before
    public void setUp() {
        filter = new CacheControlFilter();
    }

    @Test
    public void testHeaderContainsCacheMaxAge() throws IOException, ServletException {
        filter.doFilter(request, response, chain);
        verify(response).setHeader("Cache-Control", "max-age=30");
        verify(response).setHeader("Vary", "Accept");
        verify(chain).doFilter(request, response);
    }

}
