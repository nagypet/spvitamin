/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.security.auth.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

/**
 * @author Peter Nagy
 */

public class TokenClaims extends DefaultClaims {

    private static final String AUTHORITIES = "rls";
    private static final String USERID = "uid";
    private static final String LDAP = "ldap";
    private static final String DOMAIN = "domain";
    private static final String ROLES = "roles";


    public TokenClaims(Claims claims) {
        super(claims);
    }


    public TokenClaims(long userId, Collection<? extends GrantedAuthority> authorities, String ldapUrl, String domain) {
        this.setUserId(userId);
        this.setAuthorities(authorities);
        this.setLdapUrl(ldapUrl);
        this.setDomain(domain);
    }

    public TokenClaims(long userId, Collection<? extends GrantedAuthority> authorities, String ldapUrl, String domain, Set<String> roles) {
        this.setUserId(userId);
        this.setAuthorities(authorities);
        this.setLdapUrl(ldapUrl);
        this.setDomain(domain);
        this.setRoles(roles);
    }


    public long getUserId() {
        return this.get(USERID, Long.class);
    }


    public void setUserId(long userId) {
        this.put(USERID, userId);
    }


    public Collection<GrantedAuthority> getAuthorities() {
        List authorities = this.get(AUTHORITIES, List.class);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Object authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.toString()));
        }

        return grantedAuthorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authorities));
    }

    public void setLdapUrl(String ldapUrl){
        this.put(LDAP, ldapUrl);
    }

    public String getLdapUrl(){
        return this.get(LDAP, String.class);
    }

    public void setDomain(String domain){
        this.put(DOMAIN, domain);
    }

    public String getDomain(){
        return this.get(DOMAIN, String.class);
    }

    public void setRoles(Set<String> roles){
        this.put(ROLES, roles);
    }

    public List<String> getRoles(){
        return this.get(ROLES, List.class);
    }
}
