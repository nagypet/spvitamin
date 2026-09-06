package hu.perit.spvitamin.spring.security;

import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
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
 * The /authenticate endpoint should create a new session only if the request contains Basic authentication or an Oauth2 token.
 * In other cases it should behave statelessly.
 * <p>
 * If called with a refresh token, no session should be created; otherwise the session validity could not be checked.
 */
@Slf4j
public class SelectiveSessionSecurityContextRepositor implements SecurityContextRepository
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
        if (Strings.CI.startsWith(auth, "basic "))
        {
            log.trace("servletPath: {}, with basic authentication => using session repo", request.getServletPath());
            return sessionRepo;
        }
        // Only check the session for OAuth2 if there is no Authorization header,
        // since loading the session is relatively expensive and unnecessary for JWT/Bearer requests.
        if (auth == null && isOAuth2Authentication(request))
        {
            log.trace("servletPath: {}, with OAuth2 in the session => using session repo", request.getServletPath());
            return sessionRepo;
        }
        log.trace("servletPath: {}, no authentication or other type of authentication => using stateless repo", request.getServletPath());
        return statelessRepo;
    }


    private boolean isOAuth2Authentication(HttpServletRequest request)
    {
        DeferredSecurityContext deferredSecurityContext = sessionRepo.loadDeferredContext(request);
        SecurityContext securityContext = deferredSecurityContext.get();
        Authentication authentication = securityContext.getAuthentication();
        AuthorizationService authorizationService = SpringContext.getBean(AuthorizationService.class);

        return authorizationService.isOAuth2Authentication(authentication);
    }
}
