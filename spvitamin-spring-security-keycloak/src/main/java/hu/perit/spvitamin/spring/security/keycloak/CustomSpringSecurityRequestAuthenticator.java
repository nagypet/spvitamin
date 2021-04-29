package hu.perit.spvitamin.spring.security.keycloak;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.BasicAuthRequestAuthenticator;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.authentication.SpringSecurityRequestAuthenticator;

public class CustomSpringSecurityRequestAuthenticator extends SpringSecurityRequestAuthenticator
{

    public CustomSpringSecurityRequestAuthenticator(HttpFacade facade, HttpServletRequest request, KeycloakDeployment deployment,
        AdapterTokenStore tokenStore, int sslRedirectPort)
    {
        super(facade, request, deployment, tokenStore, sslRedirectPort);
    }

    @Override
    protected BasicAuthRequestAuthenticator createBasicAuthAuthenticator()
    {
        return new CustomBasicAuthRequestAuthenticator(deployment);
    }
}
