package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class AuthenticationManagerConfig
{
    @Bean
    public AuthenticationManager authenticationManager()
    {
        Map<String, ? extends AuthenticationProvider> providersMap = SpringContext.getBeansOfType(AuthenticationProvider.class);
        List<AuthenticationProvider> providers = providersMap.values().stream()
                .map(AuthenticationProvider.class::cast)
                .sorted(OrderComparator.INSTANCE)
                .toList();

        log.debug("AuthenticationManager initialized with {}", providersMap.keySet());

        ProviderManager manager = new ProviderManager(providers);
        manager.setEraseCredentialsAfterAuthentication(true);
        return manager;
    }
}
