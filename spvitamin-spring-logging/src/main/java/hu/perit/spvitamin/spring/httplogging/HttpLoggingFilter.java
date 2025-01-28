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

import hu.perit.spvitamin.core.StackTracer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class HttpLoggingFilter implements Filter
{
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if (!(log.isDebugEnabled() || log.isInfoEnabled()))
        {
            chain.doFilter(request, response);
        }
        else
        {
            try
            {
                String contentType = request.getContentType();
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                BufferedResponseWrapper bufferedResponse = new BufferedResponseWrapper((HttpServletResponse) response);
                if (contentType == null || contentType.startsWith("application/json"))
                {
                    httpServletRequest = new HttpLoggingFilter.BufferedRequestWrapper((HttpServletRequest) request);
                }

                StringBuilder logMessage = new StringBuilder();

                // Logging request
                try
                {
                    this.prepareLogMessageRequest(logMessage, httpServletRequest);
                    if (log.isInfoEnabled())
                    {
                        log.info(logMessage.toString());
                    }
                    else
                    {
                        log.debug(logMessage.toString());
                    }
                }
                catch (Throwable ex)
                {
                    log.error(StackTracer.toString(ex));
                }

                chain.doFilter(httpServletRequest, bufferedResponse);

                // Logging response
                try
                {
                    logMessage = new StringBuilder();
                    this.prepareLogMessageResponse(logMessage, httpServletRequest, bufferedResponse);
                    if (log.isInfoEnabled())
                    {
                        log.info(logMessage.toString());
                    }
                    else
                    {
                        log.debug(logMessage.toString());
                    }
                }
                catch (Throwable ex)
                {
                    log.error(StackTracer.toString(ex));
                }
            }
            finally
            {
                ThreadContext.remove("conversation");
            }
        }
    }


    private void prepareLogMessageRequest(StringBuilder logMessage, HttpServletRequest bufferedRequest) throws IOException
    {
        String body;
        if (bufferedRequest instanceof HttpLoggingFilter.BufferedRequestWrapper requestWrapper)
        {
            body = requestWrapper.getRequestBody();
        }
        else
        {
            body = "Non JSON Data";
        }

        logMessage
            .append("==> HTTP REQUEST - ")
            .append("[REMOTE ADDRESS: ").append(bufferedRequest.getRemoteAddr()).append("] ")
            .append("[HTTP METHOD: ").append(bufferedRequest.getMethod()).append("] ")
            .append("[REQUEST URL: ").append(bufferedRequest.getRequestURL()).append("] ")
            .append("[REQUEST HEADERS: ").append(this.getRequestHeaderAsString(bufferedRequest)).append("] ")
            .append("[REQUEST PARAMETERS: ").append(this.getParameterAsString(bufferedRequest)).append("] ");

        if (log.isDebugEnabled())
        {
            logMessage.append("[REQUEST BODY: ").append(body).append("]");
        }
    }


    private void prepareLogMessageResponse(StringBuilder logMessage, HttpServletRequest bufferedRequest, BufferedResponseWrapper bufferedResponse)
    {
        String contenttype = bufferedResponse.getContentType();
        String body = contenttype != null && !contenttype.startsWith("application/json") ? "Non JSON Data" : bufferedResponse.getContent();
        int httpStatus = bufferedResponse.getStatus();

        logMessage
            .append("<== HTTP RESPONSE - ")
            .append("[REMOTE ADDRESS: ").append(bufferedRequest.getRemoteAddr()).append("] ")
            .append("[HTTP METHOD: ").append(bufferedRequest.getMethod()).append("] ")
            .append("[REQUEST URL: ").append(bufferedRequest.getRequestURL()).append("] ")
            .append("[RESPONSE STATUS: ").append(httpStatus).append("] ")
            .append("[RESPONSE HEADERS: ").append(this.getResponseHeaderAsString(bufferedResponse)).append("] ");

        if (log.isDebugEnabled())
        {
            logMessage.append("[RESPONSE BODY: ").append(body).append("]");
        }
    }


    private String getRequestHeaderAsString(HttpServletRequest request)
    {
        return LoggingHelper.getHeadersAsString(new HttpRequestWrapper(request));
    }


    private String getResponseHeaderAsString(HttpServletResponse response)
    {
        return LoggingHelper.getHeadersAsString(new HttpResponseWrapper(response));
    }


    private String getParameterAsString(HttpServletRequest request)
    {
        Set<String> parameterAsString = new HashSet<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames != null && parameterNames.hasMoreElements())
        {
            StringBuilder sb = new StringBuilder();
            String parameterName = parameterNames.nextElement();
            sb.append(parameterName).append("=").append(request.getParameter(parameterName));
            parameterAsString.add(sb.toString());
        }

        return Strings.join(parameterAsString, ',');
    }


    private static final class BufferedRequestWrapper extends HttpServletRequestWrapper
    {

        private ByteArrayInputStream bais = null;
        private ByteArrayOutputStream baos = null;
        private BufferedServletInputStream bsis = null;
        private byte[] buffer = null;


        public BufferedRequestWrapper(HttpServletRequest req) throws IOException
        {
            super(req);
            InputStream is = req.getInputStream();
            this.baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            int read;
            while ((read = is.read(buf)) > 0)
            {
                this.baos.write(buf, 0, read);
            }

            this.buffer = this.baos.toByteArray();
        }


        @Override
        public ServletInputStream getInputStream()
        {
            this.bais = new ByteArrayInputStream(this.buffer);
            this.bsis = new BufferedServletInputStream(this.bais);
            return this.bsis;
        }


        String getRequestBody() throws IOException
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
            String line = null;
            StringBuilder inputBuffer = new StringBuilder();

            do
            {
                line = reader.readLine();
                if (null != line)
                {
                    inputBuffer.append(line.trim());
                }
            }
            while (line != null);

            reader.close();
            return inputBuffer.toString().trim();
        }
    }
}
