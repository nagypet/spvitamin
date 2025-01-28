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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalUserService implements UserDetailsService
{
    private final LocalUserProperties localUserProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        if (!this.localUserProperties.getLocaluser().containsKey(username))
        {
            throw new UsernameNotFoundException(username);
        }

        LocalUserProperties.User user = this.localUserProperties.getLocaluser().get(username);
        String password;
        if (user.getEncryptedPassword() != null)
        {
            CryptoUtil crypto = new CryptoUtil();

            password = crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), user.getEncryptedPassword());
        }
        else
        {
            password = user.getPassword();
        }

        return User.withUsername(username)
                .password(this.passwordEncoder.encode(password))
                .authorities("ROLE_EMPTY")
                .build();
    }
}
