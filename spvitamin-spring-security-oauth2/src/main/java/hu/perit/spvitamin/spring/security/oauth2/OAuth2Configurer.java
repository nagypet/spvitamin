package hu.perit.spvitamin.spring.security.oauth2;

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import hu.perit.spvitamin.spring.security.config.AuthenticationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OAuth2Configurer
{
    public static final String IGNORED = "ignored";

    private final AuthorizationService authorizationService;
    private final AuthenticationRepository authenticationRepository;
    private final SecurityProperties securityProperties;

    @PostConstruct
    private void setUp()
    {
        this.authorizationService.registerAuthenticatedUserFactory(new AuthenticatedUserFactoryForOidcUser());
        this.authorizationService.registerAuthenticatedUserFactory(new AuthenticatedUserFactoryForDefaultOAuth2User());
        Map<String, SecurityProperties.OAuth2Provider> providerMap = Optional.ofNullable(this.securityProperties.getOauth2()).map(i -> i.getProviders()).orElse(Collections.emptyMap());
        if (!providerMap.isEmpty())
        {
            providerMap.forEach((key, value) -> {
                log.info("Configuring OAuth2 provider '{}'.", key);
                this.authenticationRepository.registerAuthenticationType(new OAuth2AuthType(key, value.getDisplayName()));
            });
        }
    }


    @Bean
    public ClientRegistrationRepository clientRegistrationRepository()
    {
        Map<String, SecurityProperties.OAuth2Provider> providerMap = Optional.ofNullable(this.securityProperties.getOauth2()).map(i -> i.getProviders()).orElse(Collections.emptyMap());
        if (providerMap.isEmpty())
        {
            return new InMemoryClientRegistrationRepository(getDummyClientRegistration());
        }

        List<ClientRegistration> registrations = new ArrayList<>();
        for (Map.Entry<String, SecurityProperties.OAuth2Provider> entry : providerMap.entrySet())
        {
            SecurityProperties.OAuth2Provider provider = entry.getValue();
            String registrationId = entry.getKey();

            ClientRegistration clientRegistration = null;
            if (StringUtils.isNotBlank(provider.getIssuerUri()))
            {
                clientRegistration = ClientRegistrations.fromIssuerLocation(provider.getIssuerUri())
                        .registrationId(registrationId)
                        .clientId(provider.getClientId())
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .clientName(registrationId)
                        .clientSecret(provider.getClientSecret())
                        .scope(provider.getScopes())
                        .build();
            }
            else
            {
                // Configure with well-known endpoints
                SecurityProperties.WellKnownEndpoints wellKnownEndpoints = Optional.ofNullable(this.securityProperties.getOauth2())
                        .map(i -> i.getWellKnownEndpoints())
                        .map(i -> i.get(registrationId))
                        .orElse(null);
                if (wellKnownEndpoints == null)
                {
                    throw new IllegalStateException(MessageFormat.format("Well-known endpoints not found for ''{0}''!", registrationId));
                }
                clientRegistration = getClientConfigurationWithWellKnownEndpoints(registrationId, provider, wellKnownEndpoints);
            }
            registrations.add(clientRegistration);
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }


    private static ClientRegistration getClientConfigurationWithWellKnownEndpoints(String registrationId, SecurityProperties.OAuth2Provider provider, SecurityProperties.WellKnownEndpoints wellKnownEndpoints)
    {
        ClientRegistration clientRegistration;
        clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId(provider.getClientId())
                .clientSecret(provider.getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(provider.getRedirectUri())
                .scope(provider.getScopes())
                .authorizationUri(wellKnownEndpoints.getAuthorizationUri())
                .tokenUri(wellKnownEndpoints.getTokenUri())
                .jwkSetUri(wellKnownEndpoints.getJwkSetUri())
                .userInfoUri(wellKnownEndpoints.getUserInfoUri())
                .userNameAttributeName(wellKnownEndpoints.getUserNameAttributeName())
                .clientName(registrationId)
                .build();
        return clientRegistration;
    }


    // This is needed for the server to start when the oauth dependencies are present
    public ClientRegistration getDummyClientRegistration()
    {
        return ClientRegistration.withRegistrationId("dummy")
                .clientId(IGNORED)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri(IGNORED)
                .redirectUri(IGNORED)
                .tokenUri(IGNORED)
                .build();
    }


    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository()
    {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

}
