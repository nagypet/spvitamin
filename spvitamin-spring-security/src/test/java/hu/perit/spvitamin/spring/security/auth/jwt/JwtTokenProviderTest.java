/*
 * Copyright 2020-2025 the original author or authors.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@SpringBootTest
@ActiveProfiles({"default", "unittest"})
@TestPropertySource("classpath:application-unittest.properties")
@ContextConfiguration(classes = JwtTokenProviderTest.ContextConfiguration.class, initializers = ConfigDataApplicationContextInitializer.class)
@Slf4j
class JwtTokenProviderTest
{

    @Profile("unittest")
    @EnableConfigurationProperties
    @Configuration
    @ComponentScan(basePackages = {"hu.perit.spvitamin.spring.security.auth.jwt", "hu.perit.spvitamin.spring.config"})
    public static class ContextConfiguration
    {
    }

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void testValidToken()
    {
        log.debug("-----------------------------------------------------------------------------------------------------");
        log.debug("testValidToken()");

        final AuthorizationToken token = this.jwtTokenProvider.generateToken("nagy_peter",
            new TokenClaims(12, List.of(new SimpleGrantedAuthority("ADMIN")), "ldapUrl"));

        TokenClaims claims = new TokenClaims(this.jwtTokenProvider.getClaims(token.getJwt()));

        Assertions.assertEquals("nagy_peter", claims.getSubject());
        Collection<? extends GrantedAuthority> authorities = claims.getAuthorities();
        Assertions.assertEquals(List.of(new SimpleGrantedAuthority("ADMIN")), authorities);
    }

    @Test
    void testInvalidToken()
    {
        log.debug("-----------------------------------------------------------------------------------------------------");
        log.debug("testInvalidToken()");

        final AuthorizationToken token = this.jwtTokenProvider.generateToken("nagy_peter",
            new TokenClaims(12, List.of(new SimpleGrantedAuthority("ADMIN")), "ldapUrl"));
        Assertions.assertThrows(JwtException.class, () -> this.jwtTokenProvider.getClaims(token.getJwt().substring(2)));
    }

    private static class SpecialTokenClaims extends DefaultClaims
    {

        private static final String ROLES = "rls";
        private static final String USERID = "uid";
        private static final String LDAP = "ldap";

        public SpecialTokenClaims(Claims claims)
        {
            super(claims);
        }

        public SpecialTokenClaims(long userId, Collection<? extends GrantedAuthority> authorities, String ldapUrl)
        {
            this.setUserId(userId);
            this.setAuthorities(authorities);
            this.setLdapUrl(ldapUrl);
        }

        public long getUserId()
        {
            return this.get(USERID, Long.class);
        }

        public void setUserId(long userId)
        {
            this.put(USERID, userId);
        }

        public Collection<GrantedAuthority> getAuthorities()
        {
            List authorities = this.get(ROLES, List.class);

            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            for (Object authority : authorities)
            {
                grantedAuthorities.add(new SimpleGrantedAuthority(authority.toString()));
            }

            return grantedAuthorities;
        }

        public void setAuthorities(Collection<? extends GrantedAuthority> authorities)
        {
            this.put(ROLES, AuthorityUtils.authorityListToSet(authorities));
        }

        public void setLdapUrl(String ldapUrl)
        {
            this.put(LDAP, ldapUrl);
        }
    }

    @Test
    void testSpecializedTokenClaim()
    {
        log.debug("-----------------------------------------------------------------------------------------------------");
        log.debug("testSpecializedTokenClaim()");

        final AuthorizationToken token = this.jwtTokenProvider.generateToken("nagy_peter",
            new SpecialTokenClaims(12, List.of(new SimpleGrantedAuthority("ADMIN")), "ldapUrl"));

        SpecialTokenClaims claims = new SpecialTokenClaims(this.jwtTokenProvider.getClaims(token.getJwt()));

        Assertions.assertEquals("nagy_peter", claims.getSubject());
        Collection<? extends GrantedAuthority> authorities = claims.getAuthorities();
        Assertions.assertEquals(List.of(new SimpleGrantedAuthority("ADMIN")), authorities);
    }
}
