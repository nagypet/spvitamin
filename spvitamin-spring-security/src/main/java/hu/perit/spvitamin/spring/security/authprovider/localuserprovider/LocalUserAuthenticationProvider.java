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

package hu.perit.spvitamin.spring.security.authprovider.localuserprovider;

import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.config.LocalUserProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.CredentialType;
import hu.perit.spvitamin.spring.security.authprovider.AbstractSpvitaminBasicAuthenticationProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(annotation = EnableLocalUserAuthProvider.class)
public class LocalUserAuthenticationProvider extends AbstractSpvitaminBasicAuthenticationProvider
{
    private final LocalUserProperties localUserProperties;


    @PostConstruct
    private void init()
    {
        log.info("Initializing {}", this.getClass().getName());
    }


    @Override
    public AuthenticatedUser loadUserByUsernameAndPassword(String username, String password) throws AuthenticationException
    {
        if (!this.localUserProperties.getLocaluser().containsKey(username))
        {
            throw new UsernameNotFoundException(username);
        }

        LocalUserProperties.User user = this.localUserProperties.getLocaluser().get(username);
        String pwd;
        if (user.getEncryptedPassword() != null)
        {
            CryptoUtil crypto = new CryptoUtil();

            pwd = crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), user.getEncryptedPassword());
        }
        else
        {
            pwd = user.getPassword();
        }

        if (!Strings.CS.equals(pwd, password))
        {
            throw new BadCredentialsException("Invalid user credentials!");
        }

        return AuthenticatedUser.builder()
                .username(username)
                .displayName(username)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_EMPTY")))
                .anonymous(false)
                .source("LOCALUSER")
                .credentialType(CredentialType.BASIC)
                .build();
    }
}
