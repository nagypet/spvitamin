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

package hu.perit.spvitamin.spring.security.bruteforce;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

/**
 * Listens to Spring Security authentication events to maintain the failed-attempt counter.
 * <p>
 * Only {@link BadCredentialsException} increments the counter — {@code LockedException}
 * and {@code UsernameNotFoundException} are intentionally ignored to avoid double-counting.
 */
@RequiredArgsConstructor
public class AuthenticationEventListener
{
    private final LoginAttemptService loginAttemptService;


    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event)
    {
        if (event.getException() instanceof BadCredentialsException)
        {
            String username = event.getAuthentication().getName();
            loginAttemptService.registerFailure(username);
        }
    }


    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event)
    {
        String username = event.getAuthentication().getName();
        loginAttemptService.registerSuccess(username);
    }
}
