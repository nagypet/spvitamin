package hu.perit.spvitamin.spring.logging;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

@RequiredArgsConstructor
public class HttpRequestWrapper implements HttpWrapper
{
    private final HttpServletRequest httpRequest;

    @Override
    public Iterator<String> getHeaderNames()
    {
        return this.httpRequest.getHeaderNames().asIterator();
    }

    @Override
    public String getHeader(String name)
    {
        return this.httpRequest.getHeader(name);
    }
}
