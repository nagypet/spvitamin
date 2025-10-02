package hu.perit.spvitamin.spring.security.auth.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

class BearerTokenMaskingRequestWrapper extends HttpServletRequestWrapper
{
    BearerTokenMaskingRequestWrapper(HttpServletRequest request)
    {
        super(request);
    }


    private boolean isRemoved(String name)
    {
        if (name == null || !StringUtils.equalsIgnoreCase(name, "authorization"))
        {
            return false;
        }
        String authorization = super.getHeader("authorization");
        return authorization != null && authorization.startsWith("Bearer ");
    }


    @Override
    public String getHeader(String name)
    {
        return isRemoved(name) ? null : super.getHeader(name);
    }


    @Override
    public Enumeration<String> getHeaders(String name)
    {
        return isRemoved(name) ? Collections.emptyEnumeration() : super.getHeaders(name);
    }


    @Override
    public Enumeration<String> getHeaderNames()
    {
        List<String> names = Collections.list(super.getHeaderNames());
        names.removeIf(n -> isRemoved(n.toLowerCase()));
        return Collections.enumeration(names);
    }


    @Override
    public long getDateHeader(String name)
    {
        return isRemoved(name) ? -1L : super.getDateHeader(name);
    }


    @Override
    public int getIntHeader(String name)
    {
        return isRemoved(name) ? -1 : super.getIntHeader(name);
    }
}
