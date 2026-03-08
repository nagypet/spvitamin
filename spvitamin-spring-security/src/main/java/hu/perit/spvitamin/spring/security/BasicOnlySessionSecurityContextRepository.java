package hu.perit.spvitamin.spring.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * A repository implementation responsible for managing the {@link SecurityContext}
 * in a session or stateless manner, depending on the type of authorization
 * provided in the incoming HTTP request.
 * <p>
 * This should be used only in connection with the /authenticate endpoint, which can be called
 * with Basic or Bearer authentication, or even without any authentication at all.
 * The /authenticate endpoint should create a new session only if the request contains Basic authentication.
 * In other cases it should behave statelessly.
 * <p>
 * If called with a refresh token, no session should be created; otherwise the session validity could not be checked.
 */
@Slf4j
public class BasicOnlySessionSecurityContextRepository implements SecurityContextRepository
{
    private final SecurityContextRepository sessionRepo = new HttpSessionSecurityContextRepository();
    private final SecurityContextRepository statelessRepo = new NullSecurityContextRepository();


    @Override
    public DeferredSecurityContext loadDeferredContext(HttpServletRequest request)
    {
        return select(request).loadDeferredContext(request);
    }


    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder)
    {
        return loadDeferredContext(requestResponseHolder.getRequest()).get();
    }


    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response)
    {
        select(request).saveContext(context, request, response);
    }


    @Override
    public boolean containsContext(HttpServletRequest request)
    {
        return select(request).containsContext(request);
    }


    private SecurityContextRepository select(HttpServletRequest request)
    {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null)
        {
            log.trace("servletPath: {}, no authorization => using stateless repo", request.getServletPath());
            return statelessRepo;
        }
        else if (Strings.CI.startsWith(auth, "basic "))
        {
            log.trace("servletPath: {}, with basic authentication => using session repo", request.getServletPath());
            return sessionRepo;
        }
        log.trace("servletPath: {}, other type of authorization => using stateless repo", request.getServletPath());
        return statelessRepo;
    }
}
