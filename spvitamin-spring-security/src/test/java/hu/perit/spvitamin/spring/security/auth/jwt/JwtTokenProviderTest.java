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

import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                .username("nagy_peter")
                .authorities(List.of(new SimpleGrantedAuthority("ADMIN")))
                .source("ldapUrl")
                .userId("12")
                .build();
        final AuthorizationToken token = this.jwtTokenProvider.generateToken(authenticatedUser);

        TokenClaims claims = new TokenClaims(this.jwtTokenProvider.getClaims(token.getJwt()));

        assertThat(claims.getSubject()).isEqualTo("nagy_peter");
        Collection<? extends GrantedAuthority> authorities = claims.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ADMIN");
    }
}
