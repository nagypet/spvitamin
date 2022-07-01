package hu.perit.spvitamin.spring.security.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class LdapAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String url;

    public LdapAuthenticationToken(Object principal, Object credentials, String url) {
        super(principal, credentials);
        this.url = url;
    }

    public LdapAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String url) {
        super(principal, credentials, authorities);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
