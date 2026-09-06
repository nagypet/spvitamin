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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthenticatedUserFactoryForDefaultOAuth2User implements AuthenticatedUserFactory
{
    @Override
    public boolean canHandle(Authentication authentication)
    {
        if (authentication == null)
        {
            return false;
        }
        return authentication.getPrincipal() instanceof DefaultOAuth2User;
    }


    @Override
    public AuthenticatedUser createAuthenticatedUser(Authentication authentication)
    {
        String authorizedClientRegistrationId = "oauth2";
        if (authentication instanceof OAuth2AuthenticationToken oauthToken)
        {
            authorizedClientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof DefaultOAuth2User defaultOAuth2User)
        {
            return AuthenticatedUser.builder()
                    .username(defaultOAuth2User.getName())
                    .displayName(getAttribute(defaultOAuth2User, "name"))
                    .authorities(getRoles(defaultOAuth2User))
                    .userId(null)
                    .source(authorizedClientRegistrationId)
                    .anonymous(false)
                    .credentialType(CredentialType.OAUTH2)
                    .additionalClaim("email", getAttribute(defaultOAuth2User, "email"))
                    .additionalClaim("gender", getAttribute(defaultOAuth2User, "gender"))
                    .build();
        }

        return null;
    }


    private static String getAttribute(DefaultOAuth2User defaultOAuth2User, String attribute)
    {
        Map<String, Object> attributes = defaultOAuth2User.getAttributes();
        Object object = attributes.get(attribute);
        if (object == null)
        {
            return null;
        }
        try
        {
            return (String) object;
        }
        catch (Exception e)
        {
            return object.toString();
        }
    }


    private static List<GrantedAuthority> getRoles(DefaultOAuth2User defaultOAuth2User)
    {
        Map<String, Object> attributes = defaultOAuth2User.getAttributes();
        List<String> roles = (List<String>) attributes.get("roles");
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (roles != null)
        {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        else
        {
            authorities.add(new SimpleGrantedAuthority("ROLE_EMPTY"));
        }
        return authorities;
    }
}
