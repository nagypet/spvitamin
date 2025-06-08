/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.httplogging;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

@ExtendWith(MockitoExtension.class)
class HttpLoggingFilterTest {

    private HttpLoggingFilter filter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new HttpLoggingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testDoFilterWithJsonContent() throws IOException, ServletException {
        // Setup request with JSON content
        request.setContentType("application/json");
        request.setContent("{\"name\":\"test\",\"value\":123}".getBytes());
        request.addHeader("Authorization", "Bearer token123");
        request.addHeader("Content-Type", "application/json");

        // Execute filter
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called with a wrapper
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithNonJsonContent() throws IOException, ServletException {
        // Setup request with non-JSON content
        request.setContentType("text/plain");
        request.setContent("Plain text content".getBytes());

        // Execute filter
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithSensitiveHeaders() throws IOException, ServletException {
        // Setup request with sensitive headers
        request.addHeader("password", "secret123");
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

        // Execute filter
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithEmptyRequest() throws IOException, ServletException {
        // Execute filter with empty request
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithExceptionInChain() throws IOException, ServletException {
        // Setup request
        request.setContentType("application/json");

        // Make filterChain throw an exception
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(any(), any());

        // Execute filter and expect exception to be propagated
        assertThrows(ServletException.class, () -> {
            filter.doFilter(request, response, filterChain);
        });
    }

    @Test
    void testDoFilterWithNullContentType() throws IOException, ServletException {
        // Setup request with null content type
        request.setContentType(null);
        request.setContent("{\"name\":\"test\"}".getBytes());

        // Execute filter
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithEmptyJsonBody() throws IOException, ServletException {
        // Setup request with empty JSON body
        request.setContentType("application/json");
        request.setContent("".getBytes());

        // Execute filter
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(any(), any());
    }

    @Test
    void testDoFilterWithLongStringInJsonBody() throws IOException, ServletException {
        // Setup request with JSON content containing a long string that should be shortened
        String longString = "This is a very long string that exceeds the default maxStringLength of 100 characters in the PrinterVisitor class. It should be shortened in the logged output.";
        String jsonContent = "{\"name\":\"test\",\"longValue\":\"" + longString + "\"}";
        request.setContentType("application/json");
        request.setContent(jsonContent.getBytes());

        // Execute filter
        filter.doFilter(request, response, filterChain);

        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(any(), any());
    }
}
