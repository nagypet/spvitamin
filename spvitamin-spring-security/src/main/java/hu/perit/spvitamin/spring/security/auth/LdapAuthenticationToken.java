package hu.perit.spvitamin.spring.security.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class LdapAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String url;
    private String domain;

    public LdapAuthenticationToken(Object principal, Object credentials, String url) {
        super(principal, credentials);
        this.url = url;
    }

    public LdapAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String url) {
        super(principal, credentials, authorities);
        this.url = url;
    }

    public LdapAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String url, String domain) {
        super(principal, credentials, authorities);
        this.url = url;
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain(){
        return domain;
    }
}
