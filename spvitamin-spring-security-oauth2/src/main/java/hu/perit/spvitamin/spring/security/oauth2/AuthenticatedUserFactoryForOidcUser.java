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

package hu.perit.spvitamin.spring.security.oauth2;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.CredentialType;
import hu.perit.spvitamin.spring.security.auth.AuthenticatedUserFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthenticatedUserFactoryForOidcUser implements AuthenticatedUserFactory
{
    @Override
    public boolean canHandle(Object principal)
    {
        return principal instanceof OidcUser;
    }


    @Override
    public AuthenticatedUser createAuthenticatedUser(Object principal)
    {
        if (principal instanceof OidcUser oidcUser)
        {
            return AuthenticatedUser.builder()
                    .username(getAttribute(oidcUser, "email"))
                    .displayName(getAttribute(oidcUser, "name"))
                    .authorities(getRoles(oidcUser))
                    .userId(null)
                    .source("oauth2")
                    .anonymous(false)
                    .credentialType(CredentialType.OAUTH2)
                    .build();
        }

        return null;
    }


    private static String getAttribute(OidcUser oidcUser, String attribute)
    {
        Map<String, Object> attributes = oidcUser.getAttributes();
        return (String) attributes.get(attribute);
    }


    private static List<GrantedAuthority> getRoles(OidcUser oidcUser)
    {
        Set<String> originalAuthorities = AuthorityUtils.authorityListToSet(oidcUser.getAuthorities());
        Map<String, Object> attributes = oidcUser.getAttributes();
        List<String> roles = (List<String>) attributes.get("roles");
        if (roles != null)
        {
            originalAuthorities.add("ROLE_EMPTY");
            originalAuthorities.addAll(roles);
            return AuthorityUtils.createAuthorityList(originalAuthorities.toArray(new String[0]));
        }

        originalAuthorities.add("ROLE_EMPTY");
        return AuthorityUtils.createAuthorityList(originalAuthorities.toArray(new String[0]));
    }
}
