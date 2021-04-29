package hu.perit.spvitamin.spring.security.keycloak;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.authentication.RequestAuthenticatorFactory;

public class CustomSpringSecurityRequestAuthenticatorFactory implements RequestAuthenticatorFactory
{

    @Override
    public RequestAuthenticator createRequestAuthenticator(HttpFacade facade, HttpServletRequest request, KeycloakDeployment deployment,
        AdapterTokenStore tokenStore, int sslRedirectPort)
    {
        return new CustomSpringSecurityRequestAuthenticator(facade, request, deployment, tokenStore, sslRedirectPort);
    }

}
