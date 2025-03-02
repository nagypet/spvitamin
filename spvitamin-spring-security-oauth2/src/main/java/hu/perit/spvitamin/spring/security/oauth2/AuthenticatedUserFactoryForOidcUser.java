package hu.perit.spvitamin.spring.security.oauth2;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthenticatedUserFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticatedUserFactoryForOidcUser implements AuthenticatedUserFactory
{
    @Override
    public boolean canHandle(Object principal)
    {
        return principal instanceof OidcUser;
    }

    @Override
    public AuthenticatedUser createAuthenticatedUser(Object principal)
    {
        if (principal instanceof OidcUser oidcUser)
        {
            return AuthenticatedUser.builder()
                    .username(getName(oidcUser))
                    .authorities(getRoles(oidcUser))
                    .userId(-1)
                    .anonymous(false).build();
        }

        return null;
    }

    private static String getName(OidcUser oidcUser)
    {
        Map<String, Object> attributes = oidcUser.getAttributes();
        return (String) attributes.get("name");
    }


    private static List<GrantedAuthority> getRoles(OidcUser oidcUser)
    {
        Map<String, Object> attributes = oidcUser.getAttributes();
        List<String> roles = (List<String>) attributes.get("roles");
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null)
        {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        else
        {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPTY"));
        }
        return authorities;
    }
}
