package hu.perit.spvitamin.spring.security.oauth2;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthenticatedUserFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticatedUserFactoryForDefaultOAuth2User implements AuthenticatedUserFactory
{
    @Override
    public boolean canHandle(Object principal)
    {
        return principal instanceof DefaultOAuth2User;
    }

    @Override
    public AuthenticatedUser createAuthenticatedUser(Object principal)
    {
        if (principal instanceof DefaultOAuth2User defaultOAuth2User)
        {
            return AuthenticatedUser.builder()
                    .username(getName(defaultOAuth2User, "email"))
                    .displayName(getName(defaultOAuth2User, "name"))
                    .authorities(getRoles(defaultOAuth2User))
                    .userId(-1)
                    .source("oauth2")
                    .anonymous(false).build();
        }

        return null;
    }

    private static String getName(DefaultOAuth2User defaultOAuth2User, String attribute)
    {
        Map<String, Object> attributes = defaultOAuth2User.getAttributes();
        return (String) attributes.get(attribute);
    }


    private static List<GrantedAuthority> getRoles(DefaultOAuth2User defaultOAuth2User)
    {
        Map<String, Object> attributes = defaultOAuth2User.getAttributes();
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
