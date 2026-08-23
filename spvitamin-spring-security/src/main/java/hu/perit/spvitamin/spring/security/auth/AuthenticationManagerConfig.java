package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.OrderComparator;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuthenticationManagerConfig
{
    private final ApplicationEventPublisher applicationEventPublisher;


    static class OnAuthorizationServerOrResourceServerCondition implements Condition, ConfigurationCondition
    {
        @Override
        public ConfigurationPhase getConfigurationPhase()
        {
            return ConfigurationPhase.REGISTER_BEAN;
        }


        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata)
        {
            String defaultMode = SecurityProperties.Mode.AUTHORIZATION_SERVER.name();
            String mode = context.getEnvironment().getProperty("security.mode", defaultMode);
            return Strings.CI.equalsAny(mode, SecurityProperties.Mode.AUTHORIZATION_SERVER.name(), SecurityProperties.Mode.RESOURCE_SERVER.name());
        }
    }


    @Bean
    @Conditional(OnAuthorizationServerOrResourceServerCondition.class)
    public AuthenticationManager authenticationManager()
    {
        Map<String, ? extends AuthenticationProvider> providersMap = SpringContext.getBeansOfType(AuthenticationProvider.class);
        List<AuthenticationProvider> providers = providersMap.values().stream()
                .map(AuthenticationProvider.class::cast)
                .sorted(OrderComparator.INSTANCE)
                .toList();

        log.debug("AuthenticationManager initialized with {}", providersMap.keySet());

        ProviderManager manager = new ProviderManager(providers);
        manager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(applicationEventPublisher));
        manager.setEraseCredentialsAfterAuthentication(true);
        return manager;
    }
}
