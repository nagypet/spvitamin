package hu.perit.spvitamin.spring.security.authprovider;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.AuthenticationException;

public interface SpvitaminBasicAuthenticationProvider extends AuthenticationProvider
{
    AuthenticatedUser loadUserByUsernameAndPassword(String username, String password) throws AuthenticationException;
}
