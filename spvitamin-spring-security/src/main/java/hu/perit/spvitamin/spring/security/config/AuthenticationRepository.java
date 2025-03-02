package hu.perit.spvitamin.spring.security.config;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AuthenticationRepository
{
    private final List<AuthenticationType> authenticationTypes = new ArrayList<>();

    public void registerAuthenticationType(final AuthenticationType authenticationType)
    {
        this.authenticationTypes.add(authenticationType);
    }

    public List<AuthenticationType> getAuthenticationTypes()
    {
        return authenticationTypes.stream().sorted(Comparator.comparing(AuthenticationType::getLabel)).toList();
    }
}
