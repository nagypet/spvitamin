package hu.perit.spvitamin.spring.security.keycloak;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import hu.perit.spvitamin.spring.config.SpringContext;

public class CustomKeycloakAuthenticationEntryPoint extends KeycloakAuthenticationEntryPoint
{

    public CustomKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext, RequestMatcher apiRequestMatcher)
    {
        super(adapterDeploymentContext, apiRequestMatcher);
    }

    public CustomKeycloakAuthenticationEntryPoint(AdapterDeploymentContext adapterDeploymentContext)
    {
        super(adapterDeploymentContext);
    }


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException, ServletException
    {
        super.commence(request, response, authException);
        HandlerExceptionResolver resolver = SpringContext.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
        if (resolver != null)
        {
            resolver.resolveException(request, response, null, authException);
        }
    }
}
