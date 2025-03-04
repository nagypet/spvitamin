package hu.perit.spvitamin.spring.security.oauth2.rest.controller;

import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.info.RequestInfoService;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import hu.perit.spvitamin.spring.security.oauth2.rest.api.OAuthProxyApi;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.MessageFormat;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OAuthProxyController implements OAuthProxyApi
{
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;
    private final RequestInfoService requestInfoService;

    @Override
    @LoggedRestMethod(eventId = 1, subsystem = "hu.perit.spvitamin.spring.security.oauth2")
    public void authorization(String provider) throws ResourceNotFoundException, IOException
    {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);

        if (clientRegistration == null)
        {
            throw new ResourceNotFoundException(MessageFormat.format("Provider {0} not found!", provider));
        }

        String referer = httpServletRequest.getHeader("Referer");
        if (referer == null)
        {
            referer = this.requestInfoService.getBaseUrl();
        }

        log.info("Authorization request from: {}", referer);

        // Saving the referer in the oauth2_state cookie
        Cookie stateCookie = new Cookie("oauth2_state", referer);
        stateCookie.setHttpOnly(true);
        stateCookie.setPath("/");
        stateCookie.setMaxAge(300);
        httpServletResponse.addCookie(stateCookie);

        // OAuth2 authorization URL
        String authUrl = this.requestInfoService.getBaseUrl() + "/oauth2/authorization/" + provider;

        log.info("Redirecting to the authorization URL: {}", authUrl);
        this.httpServletResponse.sendRedirect(authUrl);
    }
}
