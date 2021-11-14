package hu.perit.spvitamin.spring.httplogging;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

@RequiredArgsConstructor
class HttpResponseWrapper implements HttpWrapper
{
    private final HttpServletResponse httpResponse;

    @Override
    public Iterator<String> getHeaderNames()
    {
        return this.httpResponse.getHeaderNames().iterator();
    }

    @Override
    public String getHeader(String name)
    {
        return this.httpResponse.getHeader(name);
    }
}
