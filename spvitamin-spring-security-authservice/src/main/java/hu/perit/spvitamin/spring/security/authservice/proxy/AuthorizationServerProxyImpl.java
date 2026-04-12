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

package hu.perit.spvitamin.spring.security.authservice.proxy;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.MicroserviceCollectionProperties;
import hu.perit.spvitamin.spring.feignclients.MirroringRequestInterceptor;
import hu.perit.spvitamin.spring.feignclients.SimpleFeignClientBuilder;
import hu.perit.spvitamin.spring.http.ResponseEntityUtils;
import hu.perit.spvitamin.spring.rest.api.AuthApi;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import hu.perit.spvitamin.spring.security.auth.proxy.AuthorizationServerProxy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServerProxyImpl implements AuthorizationServerProxy
{
    // RFC 7230 hop-by-hop és egyéb, általában nem továbbítandó fejlécek
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade"
    );
    public static final String SESSION = "SESSION";
    public static final String IDP_SESSION = "IDP_SESSION";

    private final MicroserviceCollectionProperties microserviceCollectionProperties;
    private final JwtTokenProvider tokenProvider;
    private final RestTemplate restTemplate;


    @Override
    public ResponseEntity<AuthorizationToken> authenticate(HttpServletRequest request)
    {
        String url = microserviceCollectionProperties.get("auth-service").getUrl();

        AuthApi idpAuthApi = SimpleFeignClientBuilder.newInstance()
                .requestInterceptor(new MirroringRequestInterceptor(getHeaders(request)))
                .exposeSetCookieHeaders(true)
                .build(AuthApi.class, url);

        ResponseEntity<AuthorizationToken> authResponse = idpAuthApi.authenticateUsingGET(null);
        // Rename the session cookie of the authorization server to IDP_SESSION to avoid conflicts with spring session management
        HttpHeaders proxiedHeaders = alterSetCookieHeader(authResponse.getHeaders());
        AuthorizationToken authorizationToken = ResponseEntityUtils.get(authResponse);
        // Update session timeout to match the token expiry time
        log.debug("Successfully got token, valid before {}", authorizationToken.getExp());
        // We use here the iat and not the 'now' because the other computers clock may not be synchronized
        Duration ttl = Duration.ofSeconds(authorizationToken.getExp().minusSeconds(authorizationToken.getIat().getEpochSecond()).getEpochSecond());
        tokenProvider.setSessionTimeout(ttl);
        return new ResponseEntity<>(authorizationToken, proxiedHeaders, authResponse.getStatusCode());
    }


    @Override
    public ResponseEntity<Void> logout(HttpServletRequest request)
    {
        HttpEntity<Void> entity = new HttpEntity<>(null, getHeaders(request));

        String url = getAuthServiceUrl("/api/spvitamin/logout");
        ResponseEntity<Void> idpResponse = this.restTemplate.exchange(
                url, HttpMethod.POST, entity, Void.class);

        HttpHeaders proxiedHeaders = alterSetCookieHeader(idpResponse.getHeaders());
        return new ResponseEntity<>(null, proxiedHeaders, idpResponse.getStatusCode());
    }


    private String getAuthServiceUrl(String path)
    {
        String base = this.microserviceCollectionProperties.get("auth-service").getUrl();
        return base + path;
    }


    private static HttpHeaders alterSetCookieHeader(HttpHeaders headers)
    {
        HttpHeaders result = new HttpHeaders();
        result.putAll(headers);

        // A fejlécnév case-insensitive, de használjuk a konstansot a konzisztencia kedvéért
        List<String> original = headers.getOrEmpty(HttpHeaders.SET_COOKIE);
        if (original.isEmpty())
        {
            return result;
        }

        List<String> updated = original.stream().map(AuthorizationServerProxyImpl::renameSessionCookie).toList();

        // Csak akkor írjuk vissza, ha ténylegesen történt módosítás
        if (!updated.equals(original))
        {
            result.put(HttpHeaders.SET_COOKIE, updated);
        }

        return result;
    }


    private static String renameSessionCookie(String cookieValue)
    {
        if (cookieValue == null)
        {
            return null;
        }
        int eq = cookieValue.indexOf('=');
        if (eq <= 0)
        {
            // Nem szabályos Set-Cookie, vagy nincs név=érték forma -> változatlanul hagyjuk
            return cookieValue;
        }

        String name = cookieValue.substring(0, eq).trim();
        if (!name.equals(SESSION))
        {
            return cookieValue;
        }

        // A név után minden (érték + attribútumok) változatlanul megmarad
        return IDP_SESSION + cookieValue.substring(eq);
    }


    private HttpHeaders getHeaders(HttpServletRequest request)
    {
        HttpHeaders springHeaders = new HttpHeaders();

        if (request != null)
        {
            Enumeration<String> names = request.getHeaderNames();
            if (names != null)
            {
                while (names.hasMoreElements())
                {
                    String name = names.nextElement();
                    String n = name.toLowerCase(java.util.Locale.ROOT);

                    // Kihagyjuk a hop-by-hop és menedzselt fejléceket
                    if (HOP_BY_HOP.contains(n) || "host".equals(n) || "content-length".equals(n))
                    {
                        continue;
                    }

                    List<String> values = Collections.list(request.getHeaders(name));
                    for (String value : values)
                    {
                        if (isSafeHeaderValue(value))
                        {
                            springHeaders.add(name, value);
                        }
                    }
                }
            }
        }

        // Putting back the IDP_SESSION cookie into SESSION for authentication
        if (springHeaders.containsKey(HttpHeaders.COOKIE))
        {
            List<String> cookieHeaders = new ArrayList<>(springHeaders.getOrEmpty(HttpHeaders.COOKIE));

            Map<String, String> cookieMap = new LinkedHashMap<>();
            for (String cookieHeader : cookieHeaders)
            {
                for (String part : cookieHeader.split(";"))
                {
                    String s = part.trim();
                    if (s.isEmpty())
                    {
                        continue;
                    }

                    int eq = s.indexOf('=');
                    if (eq > 0)
                    {
                        String key = s.substring(0, eq);
                        String val = s.substring(eq + 1);
                        cookieMap.put(key, val);
                    }
                }
            }

            if (cookieMap.containsKey(IDP_SESSION))
            {
                cookieMap.put(SESSION, cookieMap.get(IDP_SESSION));
                cookieMap.remove(IDP_SESSION);
            }

            // Újraépítjük a Cookie fejléceket (egy cookie / header sor, mint eddig)
            List<String> rebuilt = cookieMap.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .filter(this::isSafeHeaderValue)
                    .toList();

            springHeaders.put(HttpHeaders.COOKIE, rebuilt);
        }

        return springHeaders;
    }


    private boolean isSafeHeaderValue(String value)
    {
        if (value == null)
        {
            return false;
        }
        // Alapvető header injection védelem
        return value.indexOf('\r') < 0 && value.indexOf('\n') < 0;
    }
}
