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

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.domainuser.DomainUser;
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.JwtProperties;
import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.exception.InvalidTokenException;
import hu.perit.spvitamin.spring.info.CookieHelper;
import hu.perit.spvitamin.spring.info.RequestQuery;
import hu.perit.spvitamin.spring.keystore.KeystoreUtils;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.session.registry.AdvancedSessionRegistry;
import hu.perit.spvitamin.spring.session.strategy.SpvitaminCompositeSessionAuthenticationStrategy;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

/**
 * @author Peter Nagy
 */

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider
{
    public static final String HIDDEN = "hidden";

    private final SecurityProperties securityProperties;


    @RequiredArgsConstructor
    @Getter
    public enum Type
    {
        JWT("jwt"),
        ACCESS("at+jwt"),
        REFRESH("rt+jwt");

        private final String value;


        public static Optional<Type> fromValue(String value)
        {
            return Arrays.stream(Type.values()).filter(i -> i.getValue().equals(value)).findFirst();
        }
    }


    private final JwtProperties jwtProperties;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final AdvancedSessionRegistry sessionRegistry;
    private final HttpServletRequest request;


    public ResponseEntity<AuthorizationToken> generateToken(AuthenticatedUser authenticatedUser)
    {
        Instant issuedAt = Instant.now();
        Duration ttl = jwtProperties.getExpiration();
        Duration refreshTtl = jwtProperties.getRefreshExpiration();

        SecurityProperties.AuthConfiguration auth = this.securityProperties.getAuth();

        // Creating the jwt token
        AuthorizationToken jwtToken = this.generateToken(Type.JWT, authenticatedUser, auth.getClientId(), Collections.emptySet(), issuedAt, ttl);

        // Creating the refresh token
        AuthorizationToken refreshToken = this.generateToken(Type.REFRESH, authenticatedUser, auth.getClientId(), Collections.emptySet(), issuedAt, refreshTtl);

        jwtToken.setExt(Map.of("rtiat", refreshToken.getIat(), "rtexp", refreshToken.getExp()));

        // Putting tokens into the cookie
        HttpHeaders headers = new HttpHeaders();
        if (!auth.isAllowTokenInResponse() && RequestQuery.isFromBrowser())
        {
            headers.add(SET_COOKIE, CookieHelper.buildSetTokenCookie(request, jwtToken.getJwt(), auth.getAccessTokenCookieName(), ttl).toString());
            jwtToken.setJwt(HIDDEN);
        }
        headers.add(SET_COOKIE, CookieHelper.buildSetTokenCookie(request, refreshToken.getJwt(), auth.getRefreshTokenCookieName(), refreshTtl).toString());

        return new ResponseEntity<>(jwtToken, headers, HttpStatus.OK);
    }


    public AuthorizationToken generateToken(Type type, AuthenticatedUser authenticatedUser, String clientId, Set<String> scopes, Instant issuedAt, Duration ttl)
    {
        try
        {
            DomainUser domainUser = DomainUser.newInstance(authenticatedUser.getUsername());

            if (type == Type.REFRESH)
            {
                // Update session timeout
                setSessionTimeout(ttl);
                touchSession(type);
            }

            // Updating session-registry
            if (this.sessionAuthenticationStrategy instanceof SpvitaminCompositeSessionAuthenticationStrategy authenticationStrategy)
            {
                // Checking if the current user exceeded the max. session count to cover the cases when the session has been created for another user
                this.sessionRegistry.updatePrincipal(RequestQuery.getSessionId(), authenticatedUser,
                        () -> authenticationStrategy.onSessionPrincipalChanged(SecurityContextHolder.getContext().getAuthentication(), RequestQuery.getHttpServletRequest(), null));
            }
            else
            {
                this.sessionRegistry.updatePrincipal(RequestQuery.getSessionId(), authenticatedUser, null);
            }

            AuthorizationToken authorizationToken = AuthorizationToken.builder()
                    .type(type)
                    .sub(domainUser.getCanonicalName())
                    .preferredUsername(authenticatedUser.getDisplayName())
                    .iat(issuedAt)
                    .exp(issuedAt.plus(ttl))
                    .uid(authenticatedUser.getUserId())
                    .clientId(type == Type.REFRESH ? clientId : null)
                    .rls(filterRoles(authenticatedUser))
                    .scope(type != Type.JWT ? filterScopes(authenticatedUser, scopes) : null)
                    .source(authenticatedUser.getSource())
                    .sid(RequestQuery.getSessionId())
                    .additionalClaims(authenticatedUser.getAdditionalClaims())
                    .credentialType(authenticatedUser.getCredentialType())
                    .build();

            String jwt = getJwtFromAuthorizationToken(authorizationToken);
            authorizationToken.setJwt(jwt);
            return authorizationToken;
        }
        catch (Exception e)
        {
            throw new JwtException("Token creation failed!", e);
        }
    }


    private static Set<String> filterRoles(AuthenticatedUser authenticatedUser)
    {
        return AuthorityUtils.authorityListToSet(authenticatedUser.getAuthorities()).stream().filter(i -> i.startsWith("ROLE_")).collect(Collectors.toSet());
    }


    private static Set<String> filterScopes(AuthenticatedUser authenticatedUser, Set<String> additionalRoles)
    {
        Set<String> filtered = AuthorityUtils.authorityListToSet(authenticatedUser.getAuthorities()).stream()
                .filter(i -> i.startsWith("SCOPE_"))
                .map(String::toLowerCase)
                .map(i -> i.substring(6))
                .collect(Collectors.toSet());
        if (additionalRoles != null)
        {
            filtered.addAll(additionalRoles);
        }
        return filtered;
    }


    public void setSessionTimeout(Duration ttl)
    {
        String sessionId = RequestQuery.getSessionId();
        this.sessionRegistry.setMaxInactiveInterval(sessionId, ttl);
    }


    public void touchSession(Type type)
    {
        if (type == Type.JWT || type == Type.REFRESH)
        {
            String sessionId = RequestQuery.getSessionId();
            this.sessionRegistry.refreshLastRequest(sessionId);
        }
    }


    public String getJwtFromAuthorizationToken(AuthorizationToken token)
    {
        if (token == null)
        {
            return null;
        }

        TokenClaims claims = new TokenClaims();
        claims.setUserId(token.getUid());
        claims.setClientId(token.getClientId());
        claims.setScope(token.getScope());
        claims.setRoles(token.getRls());
        claims.setPreferredUsername(token.getPreferredUsername());
        claims.setSource(token.getSource());
        claims.setSessionId(token.getSid());
        claims.setAdditionalClaims(token.getAdditionalClaims());

        Key privateKey = KeystoreUtils.getPrivateKey();
        return Jwts.builder()
                .header().add("typ", token.getType().getValue()).and()
                .subject(token.getSub())
                .issuedAt(Date.from(token.getIat()))
                .expiration(Date.from(token.getExp()))
                .claims(claims)
                .signWith(privateKey)
                .compact();
    }


    public AuthorizationToken getAuthorizationTokenFromJwt(String jwt)
    {
        if (StringUtils.isBlank(jwt))
        {
            return null;
        }

        Claims claims = getClaims(jwt);
        TokenClaims tokenClaims = new TokenClaims(claims);

        return AuthorizationToken.builder()
                .type(getTokenType(jwt))
                .sub(tokenClaims.get("sub", String.class))
                .preferredUsername(tokenClaims.getPreferredUsername())
                .iat(tokenClaims.getIssuedAt().toInstant())
                .exp(tokenClaims.getExpiration().toInstant())
                .uid(tokenClaims.getUserId())
                .clientId(tokenClaims.getClientId())
                .rls(tokenClaims.getRoles())
                .scope(tokenClaims.getScope())
                .source(tokenClaims.getSource())
                .sid(tokenClaims.getSessionId())
                .additionalClaims(tokenClaims.getAdditionalClaims())
                .build();
    }


    public Claims getClaims(String jwt)
    {
        try
        {
            PublicKey publicKey = KeystoreUtils.getPublicKey();
            return new DefaultClaims(Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload());
        }
        catch (ExpiredJwtException e)
        {
            throw new InvalidTokenException("JWT token expired!", e);
        }
        catch (Exception e)
        {
            throw new InvalidTokenException("JWT token parse failed!", e);
        }
    }


    public Type getTokenType(String jwt)
    {
        try
        {
            JWSObject jws = JWSObject.parse(jwt);
            JWSHeader header = jws.getHeader();
            Map<String, Object> jsonObject = header.toJSONObject();
            String tokenType = (String) jsonObject.get("typ");
            return Type.fromValue(tokenType).orElseThrow(() -> new InvalidTokenException("Invalid token type: " + tokenType));
        }
        catch (Exception e)
        {
            throw new InvalidTokenException("JWT token parse failed!", e);
        }
    }


    public boolean isExpired(String jwt)
    {
        try
        {
            Claims claims = getClaims(jwt);
            return claims.getExpiration() == null || claims.getExpiration().before(new Date());
        }
        catch (Exception e)
        {
            log.error("JWT token parse failed! {}", StackTracer.toString(e));
            return true;
        }
    }
}
