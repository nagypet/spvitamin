package hu.perit.spvitamin.spring.security.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuthenticationConfigurer
{
    private final AuthenticationRepository authenticationRepository;

    @PostConstruct
    private void setUp()
    {
        this.authenticationRepository.registerAuthenticationType(new BasicAuthType());
    }
}
