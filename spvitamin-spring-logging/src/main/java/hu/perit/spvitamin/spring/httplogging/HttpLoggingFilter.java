/*
 * Copyright 2020-2021 the original author or authors.
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
import hu.perit.spvitamin.spring.logging.HttpRequestWrapper;
import hu.perit.spvitamin.spring.logging.HttpResponseWrapper;
import hu.perit.spvitamin.spring.logging.LoggingHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Component
@Slf4j
public class HttpLoggingFilter implements Filter
{
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if (!log.isDebugEnabled())
        {
            chain.doFilter(request, response);
        }
        else
        {
            try
            {
                String contentType = request.getContentType();
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                HttpLoggingFilter.BufferedResponseWrapper bufferedResponse = new HttpLoggingFilter.BufferedResponseWrapper((HttpServletResponse) response);
                if (contentType == null || contentType.startsWith("application/json"))
                {
                    httpServletRequest = new HttpLoggingFilter.BufferedRequestWrapper((HttpServletRequest) request);
                }

                StringBuilder logMessage = new StringBuilder();

                // Logging request
                try
                {
                    this.prepareLogMessageRequest(logMessage, httpServletRequest);
                    log.debug(logMessage.toString());
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
                    log.debug(logMessage.toString());
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
        String body = "Non JSON Data found; will not be printed";
        if (bufferedRequest instanceof HttpLoggingFilter.BufferedRequestWrapper)
        {
            body = ((HttpLoggingFilter.BufferedRequestWrapper) bufferedRequest).getRequestBody();
        }

        logMessage.append(">>> HTTP REQUEST - ")
                .append("[REMOTE ADDRESS: ").append(bufferedRequest.getRemoteAddr()).append("] ")
                .append("[HTTP METHOD: ").append(bufferedRequest.getMethod()).append("] ")
                .append("[REQUEST URL: ").append(bufferedRequest.getRequestURL()).append("] ")
                .append("[REQUEST HEADERS: ").append(this.getRequestHeaderAsString(bufferedRequest)).append("] ")
                .append("[REQUEST PARAMETERS: ").append(this.getParameterAsString(bufferedRequest)).append("] ")
                .append("[REQUEST BODY: ").append(body).append("]");
    }


    private void prepareLogMessageResponse(StringBuilder logMessage, HttpServletRequest bufferedRequest, BufferedResponseWrapper bufferedResponse) throws IOException
    {
        String contenttype = bufferedResponse.getContentType();
        String body = contenttype != null && !contenttype.startsWith("application/json") ? "Non JSON Data found; will not be printed" : bufferedResponse.getContent();
        int httpStatus = bufferedResponse.getStatus();
        logMessage.append("<<< HTTP RESPONSE - ")
                .append("[REMOTE ADDRESS: ").append(bufferedRequest.getRemoteAddr()).append("] ")
                .append("[HTTP METHOD: ").append(bufferedRequest.getMethod()).append("] ")
                .append("[REQUEST URL: ").append(bufferedRequest.getRequestURL()).append("] ")
                .append("[RESPONSE STATUS: ").append(httpStatus).append("] ")
                .append("[RESPONSE HEADERS: ").append(this.getResponseHeaderAsString(bufferedResponse)).append("] ")
                .append("[RESPONSE BODY: ").append(body).append("]");
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
        Set<String> parameterAsString = new HashSet();
        Enumeration parameterNames = request.getParameterNames();

        while (parameterNames != null && parameterNames.hasMoreElements())
        {
            StringBuilder sb = new StringBuilder();
            String parameterName = (String) parameterNames.nextElement();
            sb.append(parameterName).append("=").append(request.getParameter(parameterName));
            parameterAsString.add(sb.toString());
        }

        return Strings.join(parameterAsString, ',');
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    public void destroy()
    {
    }

    public class BufferedResponseWrapper implements HttpServletResponse
    {

        HttpServletResponse original;
        HttpLoggingFilter.TeeServletOutputStream tee;
        ByteArrayOutputStream bos;

        public BufferedResponseWrapper(HttpServletResponse response)
        {
            this.original = response;
        }

        public String getContent()
        {
            return this.bos != null ? this.bos.toString() : "";
        }

        public PrintWriter getWriter() throws IOException
        {
            return this.original.getWriter();
        }

        public ServletOutputStream getOutputStream() throws IOException
        {
            if (this.tee == null)
            {
                this.bos = new ByteArrayOutputStream();
                this.tee = HttpLoggingFilter.this.new TeeServletOutputStream(this.original.getOutputStream(), this.bos);
            }

            return this.tee;
        }

        public String getCharacterEncoding()
        {
            return this.original.getCharacterEncoding();
        }

        public String getContentType()
        {
            return this.original.getContentType();
        }

        public void setCharacterEncoding(String charset)
        {
            this.original.setCharacterEncoding(charset);
        }

        public void setContentLength(int len)
        {
            this.original.setContentLength(len);
        }

        public void setContentLengthLong(long l)
        {
            this.original.setContentLengthLong(l);
        }

        public void setContentType(String type)
        {
            this.original.setContentType(type);
        }

        public void setBufferSize(int size)
        {
            this.original.setBufferSize(size);
        }

        public int getBufferSize()
        {
            return this.original.getBufferSize();
        }

        public void flushBuffer() throws IOException
        {
            this.tee.flush();
        }

        public void resetBuffer()
        {
            this.original.resetBuffer();
        }

        public boolean isCommitted()
        {
            return this.original.isCommitted();
        }

        public void reset()
        {
            this.original.reset();
        }

        public void setLocale(Locale loc)
        {
            this.original.setLocale(loc);
        }

        public Locale getLocale()
        {
            return this.original.getLocale();
        }

        public void addCookie(Cookie cookie)
        {
            this.original.addCookie(cookie);
        }

        public boolean containsHeader(String name)
        {
            return this.original.containsHeader(name);
        }

        public String encodeURL(String url)
        {
            return this.original.encodeURL(url);
        }

        public String encodeRedirectURL(String url)
        {
            return this.original.encodeRedirectURL(url);
        }

        public String encodeUrl(String url)
        {
            return this.original.encodeUrl(url);
        }

        public String encodeRedirectUrl(String url)
        {
            return this.original.encodeRedirectUrl(url);
        }

        public void sendError(int sc, String msg) throws IOException
        {
            this.original.sendError(sc, msg);
        }

        public void sendError(int sc) throws IOException
        {
            this.original.sendError(sc);
        }

        public void sendRedirect(String location) throws IOException
        {
            this.original.sendRedirect(location);
        }

        public void setDateHeader(String name, long date)
        {
            this.original.setDateHeader(name, date);
        }

        public void addDateHeader(String name, long date)
        {
            this.original.addDateHeader(name, date);
        }

        public void setHeader(String name, String value)
        {
            this.original.setHeader(name, value);
        }

        public void addHeader(String name, String value)
        {
            this.original.addHeader(name, value);
        }

        public void setIntHeader(String name, int value)
        {
            this.original.setIntHeader(name, value);
        }

        public void addIntHeader(String name, int value)
        {
            this.original.addIntHeader(name, value);
        }

        public void setStatus(int sc)
        {
            this.original.setStatus(sc);
        }

        public void setStatus(int sc, String sm)
        {
            this.original.setStatus(sc, sm);
        }

        public String getHeader(String arg0)
        {
            return this.original.getHeader(arg0);
        }

        public Collection<String> getHeaderNames()
        {
            return this.original.getHeaderNames();
        }

        public Collection<String> getHeaders(String arg0)
        {
            return this.original.getHeaders(arg0);
        }

        public int getStatus()
        {
            return this.original.getStatus();
        }
    }

    public class TeeServletOutputStream extends ServletOutputStream
    {

        private final TeeOutputStream targetStream;

        public TeeServletOutputStream(OutputStream one, OutputStream two)
        {
            this.targetStream = new TeeOutputStream(one, two);
        }

        public void write(int arg0) throws IOException
        {
            this.targetStream.write(arg0);
        }

        public void flush() throws IOException
        {
            super.flush();
            this.targetStream.flush();
        }

        public void close() throws IOException
        {
            super.close();
            this.targetStream.close();
        }

        public boolean isReady()
        {
            return false;
        }

        public void setWriteListener(WriteListener writeListener)
        {
        }
    }

    private static final class BufferedServletInputStream extends ServletInputStream
    {

        private ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais)
        {
            this.bais = bais;
        }

        public int available()
        {
            return this.bais.available();
        }

        public int read()
        {
            return this.bais.read();
        }

        public int read(byte[] buf, int off, int len)
        {
            return this.bais.read(buf, off, len);
        }

        public boolean isFinished()
        {
            return false;
        }

        public boolean isReady()
        {
            return true;
        }

        public void setReadListener(ReadListener readListener)
        {
        }
    }

    private static final class BufferedRequestWrapper extends HttpServletRequestWrapper
    {

        private ByteArrayInputStream bais = null;
        private ByteArrayOutputStream baos = null;
        private HttpLoggingFilter.BufferedServletInputStream bsis = null;
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

        public ServletInputStream getInputStream()
        {
            this.bais = new ByteArrayInputStream(this.buffer);
            this.bsis = new HttpLoggingFilter.BufferedServletInputStream(this.bais);
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