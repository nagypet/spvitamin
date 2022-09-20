package hu.perit.spvitamin.spring.security.auth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

public class LdapAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String url;
    private String domain;
    private Set<String> roles;

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

    public LdapAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String url, String domain, Set<String> roles) {
        super(principal, credentials, authorities);
        this.url = url;
        this.domain = domain;
        this.roles = roles;
    }

    public String getUrl() {
        return url;
    }

    public String getDomain(){
        return domain;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
