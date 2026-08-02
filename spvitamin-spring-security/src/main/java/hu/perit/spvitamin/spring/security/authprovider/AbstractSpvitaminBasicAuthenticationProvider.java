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

package hu.perit.spvitamin.spring.security.authprovider;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;
import hu.perit.spvitamin.spring.security.bruteforce.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Base class for {@link SpvitaminBasicAuthenticationProvider} implementations.
 * <p>
 * Provides a standard {@link #authenticate(Authentication)} template that:
 * <ol>
 *   <li>Checks whether the account is currently locked ({@link LoginAttemptService#isBlocked}).</li>
 *   <li>Delegates credential verification to {@link #loadUserByUsernameAndPassword}.</li>
 *   <li>Calls the {@link #onAuthenticationSuccess} hook for any post-authentication logic
 *       (e.g. updating last-login timestamps).</li>
 * </ol>
 * <p>
 * Subclasses only need to implement {@link #loadUserByUsernameAndPassword}.
 * Brute force protection is applied automatically when a {@link LoginAttemptService}
 * bean is present in the application context.
 */
@Slf4j
public abstract class AbstractSpvitaminBasicAuthenticationProvider implements SpvitaminBasicAuthenticationProvider
{
    @Nullable
    @Autowired(required = false)
    private LoginAttemptService loginAttemptService;


    @Override
    public final Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        String username = authentication.getName();

        if (this.loginAttemptService != null && this.loginAttemptService.isBlocked(username))
        {
            throw new LockedException("Account is temporarily locked due to too many failed login attempts");
        }

        try
        {
            AuthenticatedUser authenticatedUser = loadUserByUsernameAndPassword(username, (String) authentication.getCredentials());
            onAuthenticationSuccess(authenticatedUser);
            return new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
        }
        catch (UsernameNotFoundException e)
        {
            return null;
        }
    }


    @Override
    public boolean supports(Class<?> authentication)
    {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }


    /**
     * Called after successful authentication, before the token is returned.
     * Override to perform post-authentication work (e.g. updating last-login timestamps).
     * The default implementation does nothing.
     */
    protected void onAuthenticationSuccess(AuthenticatedUser authenticatedUser)
    {
    }
}
