package hu.perit.spvitamin.spring.security.authprovider.localuserprovider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(annotation = EnableLocalUserAuthProvider.class)
public class LocalUserAuthenticationProvider extends DaoAuthenticationProvider
{
    private final LocalUserService localUserService;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init()
    {
        log.info("Initializing {}", this.getClass().getName());
        setUserDetailsService(this.localUserService);
        setPasswordEncoder(this.passwordEncoder);
    }
}
