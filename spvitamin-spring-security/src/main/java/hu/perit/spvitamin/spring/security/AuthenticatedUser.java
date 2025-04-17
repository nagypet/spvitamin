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

package hu.perit.spvitamin.spring.security;

import hu.perit.spvitamin.spring.exception.BadTokenException;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Peter Nagy
 */


@Data
@Builder
public class AuthenticatedUser implements UserDetails
{
    @Serial
    private static final long serialVersionUID = -4734744978387700215L;

    private String username;
    private String userId;
    private String displayName;
    private Collection<? extends GrantedAuthority> authorities;
    @Builder.Default
    private boolean anonymous = true;
    private String source;
    @Singular("additionalClaim")
    private Map<String, Object> additionalClaims;


    public static AuthenticatedUser fromClaims(TokenClaims claims)
    {
        return AuthenticatedUser.builder()
                .username(claims.getSubject())
                .displayName(claims.getPreferredUsername())
                .authorities(claims.getAuthorities())
                .userId(claims.getUserId())
                .anonymous(false)
                .source(claims.getSource())
                .additionalClaims((Map<? extends String, ?>) claims.get("add"))
                .build();
    }


    public <T> T getAdditionalClaimThrow(String name, Class<T> clazz)
    {
        if (!additionalClaims.containsKey(name) || additionalClaims.get(name) == null || "null".equals(additionalClaims.get(name)))
        {
            throw new BadTokenException(MessageFormat.format("The token ''{0}'' doesn''t contain a claim with name ''{1}''", this.username, name));
        }
        return (T) additionalClaims.get(name);
    }


    public <T> Optional<T> getAdditionalClaim(String name, Class<T> clazz)
    {
        if ("null".equals(additionalClaims.get(name)))
        {
            return Optional.empty();
        }
        return (Optional<T>) Optional.ofNullable(additionalClaims.get(name));
    }


    public void putAdditionalClaim(String name, Object value)
    {
        if (this.additionalClaims == null)
        {
            this.additionalClaims = new HashMap<>();
        }
        else if (!(this.additionalClaims instanceof HashMap))
        {
            this.additionalClaims = new HashMap<>(this.additionalClaims);
        }
        this.additionalClaims.put(name, value);
    }


    public AuthenticatedUser clone()
    {
        return AuthenticatedUser.builder()
                .username(username)
                .userId(userId)
                .displayName(displayName)
                .authorities(authorities)
                .anonymous(anonymous)
                .source(source)
                .additionalClaims(additionalClaims)
                .build();
    }


    @Override
    public String getPassword()
    {
        throw new UnsupportedOperationException("getPassword()");
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
}
