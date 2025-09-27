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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Peter Nagy
 */

@NoArgsConstructor
public class TokenClaims extends DefaultClaims
{
    public static final String ROLES = "rls";
    public static final String SCOPE = "scope";
    public static final String USERID = "uid";
    public static final String CLIENTID = "client_id";
    public static final String SRC = "src";
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String SID = "sid";
    public static final String ADD = "add";


    public TokenClaims(Claims claims)
    {
        super(claims);
    }


    public String getUserId()
    {
        return this.get(USERID, String.class);
    }


    public void setUserId(String userId)
    {
        this.put(USERID, userId);
    }


    public String getClientId()
    {
        return this.get(CLIENTID, String.class);
    }


    public void setClientId(String clientId)
    {
        this.put(CLIENTID, clientId);
    }


    public String getPreferredUsername()
    {
        return this.get(PREFERRED_USERNAME, String.class);
    }


    public void setPreferredUsername(String preferredUsername)
    {
        this.put(PREFERRED_USERNAME, preferredUsername);
    }


    public Collection<GrantedAuthority> getAuthorities()
    {
        List<?> authorities = this.get(ROLES, List.class);
        return getAuthorities(authorities);
    }


    public static Collection<GrantedAuthority> getAuthorities(List<?> authorities)
    {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Object authority : authorities)
        {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.toString()));
        }

        return grantedAuthorities;
    }


    public void setAuthorities(Collection<? extends GrantedAuthority> authorities)
    {
        this.put(ROLES, new ArrayList<>(AuthorityUtils.authorityListToSet(authorities)));
    }


    public Set<String> getRoles()
    {
        List<?> authorities = this.get(ROLES, List.class);
        return authorities.stream().map(String::valueOf).collect(Collectors.toSet());
    }


    public void setRoles(Set<String> roles)
    {
        this.put(ROLES, new ArrayList<>(roles));
    }


    public Set<String> getScope()
    {
        String scope = this.get(SCOPE, String.class);
        if (StringUtils.isNotBlank(scope))
        {
            String[] split = scope.split(" ");
            return Arrays.stream(split).map(String::strip).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }


    public void setScope(Set<String> scopes)
    {
        if (scopes != null && !scopes.isEmpty())
        {
            this.put(SCOPE, String.join(" ", scopes));
        }
    }


    public String getSource()
    {
        return this.get(SRC, String.class);
    }


    public void setSource(String source)
    {
        this.put(SRC, source);
    }


    public String getSessionId()
    {
        return this.get(SID, String.class);
    }


    public void setSessionId(String sessionId)
    {
        this.put(SID, sessionId);
    }


    public Map<String, Object> getAdditionalClaims()
    {
        return (Map<String, Object>) this.get(ADD, Map.class);
    }


    public void setAdditionalClaims(Map<String, Object> additionalClaims)
    {
        if (additionalClaims != null && !additionalClaims.isEmpty())
        {
            this.put(ADD, additionalClaims);
        }
    }
}
