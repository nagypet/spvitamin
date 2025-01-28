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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

final class BufferedResponseWrapper implements HttpServletResponse
{

    HttpServletResponse original;
    TeeServletOutputStream tee;
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
            this.tee = new TeeServletOutputStream(this.original.getOutputStream(), this.bos);
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
        return this.original.encodeURL(url);
    }

    public String encodeRedirectUrl(String url)
    {
        return this.original.encodeRedirectURL(url);
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
