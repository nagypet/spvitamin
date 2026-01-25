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

import hu.perit.spvitamin.json.JSonSerializer;
import hu.perit.spvitamin.spring.exception.BadTokenException;
import hu.perit.spvitamin.spring.security.auth.jwt.TokenClaims;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import tools.jackson.core.JacksonException;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Peter Nagy
 */


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AuthenticatedUser implements UserDetails
{
    @Serial
    private static final long serialVersionUID = -4734744978387700215L;


    private String username;
    private String userId;
    private String displayName;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean anonymous = true;
    private String source;
    private Map<String, Serializable> additionalClaims;
    private CredentialType credentialType = CredentialType.UNKNOWN;


    public static Builder builder()
    {
        return new Builder();
    }


    public static AuthenticatedUser fromClaims(TokenClaims claims)
    {
        Collection<GrantedAuthority> authoritiesAndScopes = claims.getAuthorities();
        Set<String> scopes = claims.getScope().stream().map(s -> "SCOPE_" + s).collect(Collectors.toSet());
        authoritiesAndScopes.addAll(AuthorityUtils.createAuthorityList(scopes));
        return AuthenticatedUser.builder()
                .username(claims.getSubject())
                .displayName(claims.getPreferredUsername())
                .authorities(authoritiesAndScopes)
                .userId(claims.getUserId())
                .anonymous(false)
                .source(claims.getSource())
                .additionalClaims(claims.getAdditionalClaims())
                .build();
    }


    public <T> T getAdditionalClaimThrow(String name, Class<T> clazz)
    {
        return getAdditionalClaim(name, clazz)
                .orElseThrow(() -> new BadTokenException(MessageFormat.format("The token ''{0}'' doesn''t contain a claim with name ''{1}''", this.username, name)));
    }


    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAdditionalClaim(String name, Class<T> clazz)
    {
        if (additionalClaims == null || additionalClaims.get(name) == null || "null".equals(additionalClaims.get(name)))
        {
            return Optional.empty();
        }
        Object value = additionalClaims.get(name);
        if (value.getClass().isAssignableFrom(clazz))
        {
            return Optional.ofNullable((T) value); // NOSONAR
        }
        else
        {
            // Couldn't be cast, let's try to convert with Json
            try
            {
                String json = JSonSerializer.toJson(this.additionalClaims.get(name));
                return Optional.ofNullable(JSonSerializer.fromJson(json, clazz));
            }
            catch (JacksonException ex)
            {
                return Optional.empty();
            }
        }
    }


    public Builder clone()
    {
        return AuthenticatedUser.builder()
                .username(username)
                .userId(userId)
                .displayName(displayName)
                .authorities(authorities)
                .anonymous(anonymous)
                .source(source)
                .additionalClaims(additionalClaims)
                .credentialType(credentialType)
                ;
    }


    public boolean hasRole(String role)
    {
        if (getAuthorities() == null)
        {
            return false;
        }

        return getAuthorities().stream()
                .anyMatch(auth -> Objects.equals(role, auth.getAuthority()));
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


    public static class Builder
    {
        private String username;
        private String userId;
        private String displayName;
        private Collection<? extends GrantedAuthority> authorities;
        private boolean anonymous = true;
        private String source;
        private Map<String, Serializable> additionalClaims;
        private CredentialType credentialType = CredentialType.UNKNOWN;


        public Builder username(String username)
        {
            this.username = username;
            return this;
        }


        public Builder userId(String userId)
        {
            this.userId = userId;
            return this;
        }


        public Builder displayName(String displayName)
        {
            this.displayName = displayName;
            return this;
        }


        public Builder authorities(Collection<? extends GrantedAuthority> authorities)
        {
            this.authorities = authorities;
            return this;
        }


        public Builder anonymous(boolean anonymous)
        {
            this.anonymous = anonymous;
            return this;
        }


        public Builder source(String source)
        {
            this.source = source;
            return this;
        }


        public Builder additionalClaims(Map<String, Serializable> additionalClaims)
        {
            this.additionalClaims = additionalClaims;
            return this;
        }


        public Builder additionalClaim(String name, Serializable value)
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
            return this;
        }


        public Builder credentialType(CredentialType credentialType)
        {
            this.credentialType = credentialType;
            return this;
        }


        public AuthenticatedUser build()
        {
            return new AuthenticatedUser(
                    username,
                    userId,
                    displayName,
                    authorities,
                    anonymous,
                    source,
                    additionalClaims,
                    credentialType
            );
        }
    }
}
