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

package hu.perit.spvitamin.spring.security.oauth2.idp.service.impl.oauth2;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.exception.InvalidTokenException;
import hu.perit.spvitamin.spring.info.CookieHelper;
import hu.perit.spvitamin.spring.info.RequestQuery;
import hu.perit.spvitamin.spring.rolemapper.RoleMapperService;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.oauth2.idp.config.Constants;
import hu.perit.spvitamin.spring.security.oauth2.idp.config.ErrorCode;
import hu.perit.spvitamin.spring.security.oauth2.idp.config.SpvitaminOAuth2Properties;
import hu.perit.spvitamin.spring.security.oauth2.idp.registry.SpvitaminClientRegistry;
import hu.perit.spvitamin.spring.security.oauth2.idp.rest.model.ClientAuth;
import hu.perit.spvitamin.spring.security.oauth2.idp.rest.model.TokenResult;
import hu.perit.spvitamin.spring.security.oauth2.idp.service.api.OAuth2Service;
import hu.perit.spvitamin.spring.security.oauth2.idp.service.api.TokenService;
import hu.perit.spvitamin.spring.session.registry.AdvancedSessionRegistry;
import hu.perit.spvitamin.spring.session.strategy.SpvitaminCompositeSessionAuthenticationStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2ServiceImpl implements OAuth2Service
{
    private final SpvitaminOAuth2Properties spvitaminOAuth2Properties;
    private final SpvitaminClientRegistry spvitaminClientRegistry;
    private final TokenService tokenService;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;
    private final RoleMapperService roleMapperService;
    private AuthenticationManager authenticationManager;
    private final ObjectProvider<AuthenticationProvider> authenticationProviders;
    private final ObjectProvider<AuthenticationManager> authenticationManagers;
    private final AdvancedSessionRegistry sessionRegistry;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;


    @PostConstruct
    void init()
    {
        AuthenticationManager manager = this.authenticationManagers.getIfAvailable();
        this.authenticationManager = (manager != null) ? manager : this.getDefaultAuthenticationManager();
    }


    private AuthenticationManager getDefaultAuthenticationManager()
    {
        // Az összes AuthenticationProvider begyűjtése a kontextusból, rendezve (@Order/Ordered alapján)
        List<AuthenticationProvider> providers = this.authenticationProviders.orderedStream().toList();
        log.debug("AuthenticationManager initialized with {}", providers.stream().map(i -> i.getClass().getName()).toList());
        ProviderManager manager = new ProviderManager(providers);
        manager.setEraseCredentialsAfterAuthentication(true);
        return manager;
    }


    @Override
    public ResponseEntity<Map<String, Object>> token(MultiValueMap<String, String> form)
    {
        ClientAuth clientAuth = extractClientAuth(request, form);
        String grantType = one(form, "grant_type");

        SpvitaminOAuth2Properties.ClientProps clientProps = spvitaminClientRegistry.authenticate(clientAuth.getClientId(), clientAuth.getClientSecret()).orElse(null);
        if (clientProps == null)
        {
            return error(ErrorCode.INVALID_CLIENT, "Invalid client credentials.");
        }

        Set<String> requestedScopes = splitScopes(one(form, Constants.SCOPE));
        Set<String> grantedScopes = spvitaminClientRegistry.validateScopes(grantType, requestedScopes, clientProps.getScopes());

        ResponseEntity<Map<String, Object>> response;
        if (isGrantTypeEquals(grantType, Constants.CLIENT_CREDENTIALS))
        {
            response = handleClientCredentials(clientProps.getClientId(), grantedScopes);
        }
        else if (isGrantTypeEquals(grantType, Constants.REFRESH_TOKEN))
        {
            response = handleRefresh(clientProps, one(form, Constants.REFRESH_TOKEN));
        }
        else if (isGrantTypeEquals(grantType, Constants.PASSWORD))
        {
            response = handleUsernamePassword(clientProps.getClientId(), grantedScopes, form);
        }
        else
        {
            response = error(ErrorCode.UNSUPPORTED_GRANT_TYPE, "Supported: " + String.join(", ", this.spvitaminOAuth2Properties.getGrantTypes()));
        }

        // Ha a válasz tartalmaz refresh_token-t, tegyük cookie-ba is
        return finalizeWithRefreshTokenCookie(response, clientProps);
    }


    private ResponseEntity<Map<String, Object>> finalizeWithRefreshTokenCookie(ResponseEntity<Map<String, Object>> original, SpvitaminOAuth2Properties.ClientProps clientProps)
    {
        Map<String, Object> body = original.getBody();
        boolean isError = original.getStatusCode().isError() || (body != null && body.containsKey("error"));

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(original.getHeaders());

        if (isError)
        {
            headers.add(HttpHeaders.SET_COOKIE, CookieHelper.buildDeleteTokenCookie(request, clientProps.getClientId()).toString());
            return new ResponseEntity<>(body, headers, original.getStatusCode());
        }

        if (body != null)
        {
            Object rt = body.get(Constants.REFRESH_TOKEN);
            if (rt instanceof String refreshToken && !refreshToken.isBlank())
            {
                Duration refreshTtl = spvitaminOAuth2Properties.getTokens().getRefreshTtl();
                headers.add(HttpHeaders.SET_COOKIE, CookieHelper.buildSetTokenCookie(request, refreshToken, clientProps.getClientId(), refreshTtl).toString());
                if (!clientProps.isAllowRefreshTokenInResponse())
                {
                    body.remove(Constants.REFRESH_TOKEN);
                }
                return new ResponseEntity<>(body, headers, original.getStatusCode());
            }
        }

        // Sikeres, de nincs refresh_token a body-ban -> nem nyúlunk a cookie-hoz
        return original;
    }


    private boolean isGrantTypeEquals(String grantTypeRequested, String grantType)
    {
        if (StringUtils.isAnyBlank(grantTypeRequested, grantType))
        {
            return false;
        }
        return grantType.equalsIgnoreCase(grantTypeRequested)
                && this.spvitaminOAuth2Properties.getGrantTypes().contains(grantType);
    }


    @Override
    public ResponseEntity<Map<String, Object>> refresh(MultiValueMap<String, String> form)
    {
        ClientAuth clientAuth = extractClientAuth(request, form);
        SpvitaminOAuth2Properties.ClientProps clientProps = spvitaminClientRegistry.authenticate(clientAuth.getClientId(), clientAuth.getClientSecret()).orElse(null);
        if (clientProps == null)
        {
            return error(ErrorCode.INVALID_CLIENT, "Invalid client credentials.");
        }

        String refreshToken = one(form, Constants.REFRESH_TOKEN);
        ResponseEntity<Map<String, Object>> response = handleRefresh(clientProps, refreshToken);
        // Ha a válasz tartalmaz refresh_token-t, tegyük cookie-ba is
        return finalizeWithRefreshTokenCookie(response, clientProps);
    }


    @Override
    public ResponseEntity<Map<String, Object>> openidConfiguration()
    {
        String issuer = spvitaminOAuth2Properties.getIssuer();
        String base = spvitaminOAuth2Properties.getBasePath();
        return ResponseEntity.ok(Map.of(
                "issuer", issuer,
                "token_endpoint", issuer + base + "/oauth2/token",
                "userinfo_endpoint", issuer + base + "/oauth2/userinfo",
                "jwks_uri", issuer + "/.well-known/jwks.json",
                "grant_types_supported", spvitaminOAuth2Properties.getGrantTypes(),
                "token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post")
        ));
    }


    @Override
    public ResponseEntity<Map<String, Object>> getUserInfo()
    {
        Map<String, Object> retval = new HashMap<>();

        // Check scopes
        AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();
        Set<String> permissions = AuthorityUtils.authorityListToSet(authenticatedUser.getAuthorities());
        if (permissions.stream().anyMatch(i -> i.equalsIgnoreCase("SCOPE_OPENID")))
        {
            retval.put("sub", authenticatedUser.getUserId() != null ? authenticatedUser.getUserId() : authenticatedUser.getUsername());
        }
        if (permissions.stream().anyMatch(i -> i.equalsIgnoreCase("SCOPE_PROFILE")))
        {
            retval.put("name", authenticatedUser.getUsername());
            retval.put("preferred_username", authenticatedUser.getDisplayName());
        }
        return ResponseEntity.ok(retval);
    }


    private ResponseEntity<Map<String, Object>> handleUsernamePassword(String clientId, Set<String> grantedScopes, MultiValueMap<String, String> form)
    {
        if (StringUtils.isAnyBlank(one(form, Constants.USERNAME), one(form, Constants.PASSWORD)))
        {
            return error(ErrorCode.INVALID_REQUEST, "Missing credentials: username and password are required for password grant type.");
        }

        try
        {
            // 1) Programozott autentikáció ugyanazzal a lánccal (AuthenticationManager)
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(one(form, Constants.USERNAME), one(form, Constants.PASSWORD));
            Authentication authResult = this.authenticationManager.authenticate(authRequest);

            // 2) SecurityContext beállítása (mint a filterlánc tenné)
            SecurityContextHolder.getContext().setAuthentication(authResult);

            // 3) Post-auth műveletek meghívása
            AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();
            if (!authenticatedUser.isAnonymous())
            {
                Collection<? extends GrantedAuthority> groups = authenticatedUser.getAuthorities();
                Collection<GrantedAuthority> roles = this.roleMapperService.mapUsernameAndGroupToRoles(authenticatedUser.getUsername(), groups);
                authenticatedUser.setAuthorities(roles);

                log.debug(String.format("Granted roles: '%s'", authenticatedUser.getAuthorities().toString()));
                authorizationService.setAuthenticatedUser(authenticatedUser);
            }

            // 4) Updating session
            SessionInformation sessionInformation = this.sessionRegistry.getSessionInformation(RequestQuery.getSessionId());
            if (sessionInformation == null)
            {
                this.sessionAuthenticationStrategy.onAuthentication(authResult, this.request, null);
            }
            else
            {
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
            }

            // 4) Tokenek kiállítása
            Duration accessTtl = spvitaminOAuth2Properties.getTokens().getAccessTtl();
            Duration refreshTtl = spvitaminOAuth2Properties.getTokens().getRefreshTtl();

            TokenResult access = tokenService.issueAccessTokenForUser(clientId, authenticatedUser, grantedScopes, accessTtl);
            TokenResult refresh = tokenService.issueRefreshTokenForUser(clientId, authenticatedUser, grantedScopes, refreshTtl);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put(Constants.ACCESS_TOKEN, access.getToken());
            response.put(Constants.TOKEN_TYPE, Constants.BEARER);
            response.put(Constants.EXPIRES_IN, access.expiresInSeconds());
            if (grantedScopes.contains(Constants.OFFLINE_ACCESS))
            {
                response.put(Constants.REFRESH_TOKEN, refresh.getToken());
            }
            // RFC6709 szerint a response-ban érdemes visszaadni a ténylegesen megadott scope-ot
            if (!grantedScopes.isEmpty())
            {
                response.put(Constants.SCOPE, String.join(" ", grantedScopes));
            }
            return ResponseEntity.ok(response);
        }
        catch (AuthenticationException ex)
        {
            // OAuth2 szerint helyes hibakód jelszavas grantre: invalid_grant
            return error(ErrorCode.INVALID_GRANT, "Bad credentials");
        }
    }


    private ResponseEntity<Map<String, Object>> handleClientCredentials(String clientId, Set<String> grantedScopes)
    {
        Duration accessTtl = spvitaminOAuth2Properties.getTokens().getAccessTtl();
        Duration refreshTtl = spvitaminOAuth2Properties.getTokens().getRefreshTtl();

        TokenResult access = tokenService.issueAccessTokenForClient(clientId, grantedScopes, accessTtl);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(Constants.ACCESS_TOKEN, access.getToken());
        response.put(Constants.TOKEN_TYPE, Constants.BEARER);
        response.put(Constants.EXPIRES_IN, access.expiresInSeconds());
        // RFC6709 szerint a response-ban érdemes visszaadni a ténylegesen megadott scope-ot
        if (!grantedScopes.isEmpty())
        {
            response.put(Constants.SCOPE, String.join(" ", grantedScopes));
        }

        if (grantedScopes.contains(Constants.OFFLINE_ACCESS))
        {
            TokenResult refresh = tokenService.issueRefreshTokenForClient(clientId, grantedScopes, refreshTtl);
            response.put(Constants.REFRESH_TOKEN, refresh.getToken());
        }
        return ResponseEntity.ok(response);
    }


    private ResponseEntity<Map<String, Object>> handleRefresh(SpvitaminOAuth2Properties.ClientProps clientProps, String refreshTokenFromForm)
    {
        try
        {
            AuthenticatedUser authenticatedUser = this.authorizationService.getAuthenticatedUser();
            AuthorizationToken token = null;
            if (authenticatedUser.isAnonymous())
            {
                // 1) Form paraméterben próbáljuk
                String refreshToken = StringUtils.trimToNull(refreshTokenFromForm);

                // 2) Ha nincs form-ban, próbáljuk cookie-ból
                if (refreshToken == null)
                {
                    refreshToken = CookieHelper.getCookieValue(clientProps.getClientId(), request);
                }

                // 3) Ha továbbra sincs, hiba
                if (refreshToken == null)
                {
                    return error(ErrorCode.INVALID_REQUEST, "Missing refresh_token in form or cookie.");
                }

                token = tokenService.verifyRefreshToken(refreshToken);

                ResponseEntity<Map<String, Object>> validationResult = validateRefreshToken(token, clientProps);
                if (validationResult != null)
                {
                    return validationResult;
                }
            }
            else
            {
                token = this.tokenService.getSessionToken(authenticatedUser, clientProps);
            }

            Duration accessTtl = spvitaminOAuth2Properties.getTokens().getAccessTtl();
            Duration refreshTtl = spvitaminOAuth2Properties.getTokens().getRefreshTtl();
            TokenResult access = tokenService.refreshToken(token, JwtTokenProvider.Type.ACCESS, accessTtl);
            TokenResult refresh = tokenService.refreshToken(token, JwtTokenProvider.Type.REFRESH, refreshTtl);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put(Constants.ACCESS_TOKEN, access.getToken());
            response.put(Constants.TOKEN_TYPE, Constants.BEARER);
            response.put(Constants.EXPIRES_IN, access.expiresInSeconds());
            response.put(Constants.REFRESH_TOKEN, refresh.getToken());
            if (token.getScope() != null && !token.getScope().isEmpty())
            {
                response.put(Constants.SCOPE, String.join(" ", token.getScope()));
            }
            return ResponseEntity.ok(response);
        }
        catch (InvalidTokenException ex)
        {
            return error(ErrorCode.INVALID_GRANT, "Invalid refresh token.");
        }
    }


    private ResponseEntity<Map<String, Object>> validateRefreshToken(AuthorizationToken token, SpvitaminOAuth2Properties.ClientProps clientProps)
    {
        if (!Objects.equals(token.getClientId(), clientProps.getClientId()))
        {
            return error(ErrorCode.INVALID_GRANT, "Refresh token does not belong to client.");
        }

        if (token.getType() != JwtTokenProvider.Type.REFRESH)
        {
            return error(ErrorCode.INVALID_GRANT, "Refresh token is not valid.");
        }

        SessionInformation sessionInformation = this.sessionRegistry.getSessionInformation(token.getSid());
        if (sessionInformation == null || sessionInformation.isExpired())
        {
            return error(ErrorCode.INVALID_GRANT, "Session expired.");
        }

        return null;
    }


    private static Set<String> splitScopes(String scope)
    {
        if (scope == null || scope.isBlank())
        {
            return Collections.emptySet();
        }
        return Stream.of(scope.split("\\s+")).filter(s -> !s.isBlank()).collect(Collectors.toSet());
    }


    private static String one(MultiValueMap<String, String> form, String key)
    {
        List<String> vals = form.get(key);
        return (vals == null || vals.isEmpty()) ? null : vals.getFirst();
    }


    private static ResponseEntity<Map<String, Object>> error(ErrorCode errorCode, String desc)
    {
        log.debug(errorCode.getText() + " - " + desc);

        Map<String, Object> m = new HashMap<>();
        m.put("error", errorCode.getText());
        m.put("error_description", desc);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(m);
    }


    private ClientAuth extractClientAuth(HttpServletRequest request, MultiValueMap<String, String> form)
    {
        // 1) HTTP Basic
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Basic "))
        {
            byte[] decoded = Base64.getDecoder().decode(auth.substring("Basic ".length()));
            String[] parts = new String(decoded, StandardCharsets.UTF_8).split(":", 2);
            if (parts.length == 2)
            {
                return new ClientAuth(parts[0], parts[1]);
            }
        }
        // 2) client_secret_post
        String clientId = one(form, "client_id");
        String clientSecret = one(form, "client_secret");
        return new ClientAuth(clientId, clientSecret);
    }
}
