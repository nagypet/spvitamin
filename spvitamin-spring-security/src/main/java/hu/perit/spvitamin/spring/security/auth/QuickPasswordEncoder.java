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

package hu.perit.spvitamin.spring.security.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.config.SysConfig;

/**
 * @author Peter Nagy
 */


public class QuickPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            throw new BadCredentialsException("Invalid password provided!");
        }

        CryptoUtil crypto = new CryptoUtil();
        return crypto.encrypt(SysConfig.getCryptoProperties().getSecret(), rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.length() == 0) {
            throw new BadCredentialsException("Invalid raw password provided!");
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            throw new BadCredentialsException("Invalid encoded password provided!");
        }

        CryptoUtil crypto = new CryptoUtil();

        return crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), encodedPassword).equals(rawPassword.toString());
    }
}
