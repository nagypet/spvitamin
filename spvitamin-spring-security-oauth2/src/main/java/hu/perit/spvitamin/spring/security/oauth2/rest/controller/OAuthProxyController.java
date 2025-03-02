package hu.perit.spvitamin.spring.security.oauth2.rest.controller;

import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import hu.perit.spvitamin.spring.security.oauth2.rest.api.OAuthProxyApi;
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

    @Override
    @LoggedRestMethod(eventId = 1, subsystem = "hu.perit.spvitamin.spring.security.oauth2")
    public void authorization(String provider) throws ResourceNotFoundException, IOException
    {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(provider);

        if (clientRegistration == null)
        {
            throw new ResourceNotFoundException(MessageFormat.format("Provider {0} not found!", provider));
        }

        // https://login.microsoftonline.com/aaef3ba4-e05f-4282-a763-ad4698e5e531/oauth2/v2.0/authorize?
        // client_id=48de203b-3973-4e90-875c-d78b27beb2e2
        // &response_type=code
        // &redirect_uri=http://localhost:8410/login/oauth2/code/microsoft
        // &scope=openid,profile,email
//        String redirectUri = getRedirectUri(clientRegistration.getRegistrationId());
//        String authorizationUri = clientRegistration.getProviderDetails().getAuthorizationUri()
//                + "?client_id=" + clientRegistration.getClientId()
//                + "&response_type=code"
//                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
//                + "&scope=" + String.join(",", clientRegistration.getScopes())
//                ;
//
//        log.info("Authorization URI: {}", authorizationUri);
//
//        this.httpServletResponse.sendRedirect(authorizationUri);

        // https://login.microsoftonline.com/aaef3ba4-e05f-4282-a763-ad4698e5e531/oauth2/v2.0/authorize?
        // response_type=code
        // &client_id=48de203b-3973-4e90-875c-d78b27beb2e2
        // &scope=openid profile email
        // &state=0OSUNrKl3N8F3x694xk-O-_XCEG2KNKkhu7q4HAilL4=
        // &redirect_uri=http://localhost:8410/login/oauth2/code/microsoft
        // &nonce=BW4SzZq1rgl-hG05XWlPDTAF0VLK98Vv3ysnJhI9uUg
        String redirectUri = getBaseUrl() + "/oauth2/authorization/" + provider;
        log.info("OAuth2 redirect URL: {}", redirectUri);
        this.httpServletResponse.sendRedirect(redirectUri);
    }


    private String getRedirectUri(String registrationId)
    {
        String baseUrl = getBaseUrl();
        String action = "login";

        return baseUrl + "/" + action + "/oauth2/code/" + registrationId;
    }


    public String getBaseUrl()
    {
        String scheme = httpServletRequest.getScheme();
        String serverName = httpServletRequest.getServerName();
        int serverPort = httpServletRequest.getServerPort();

        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
        {
            return scheme + "://" + serverName;
        }
        else
        {
            return scheme + "://" + serverName + ":" + serverPort;
        }
    }
}
