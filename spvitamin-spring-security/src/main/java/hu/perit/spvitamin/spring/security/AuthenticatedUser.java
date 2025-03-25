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

import java.io.Serial;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Data;

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
    private long userId;
    private String displayName;
    private Collection<? extends GrantedAuthority> authorities;
    @Builder.Default
    private boolean anonymous = true;
    private String source;


    public AuthenticatedUser clone()
    {
        return AuthenticatedUser.builder()
                .username(username)
                .userId(userId)
                .displayName(displayName)
                .authorities(authorities)
                .anonymous(anonymous)
                .source(source)
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
