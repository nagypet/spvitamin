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

/**
 * Tracks failed login attempts and manages account lockouts.
 * <p>
 * The default implementation is {@link InMemoryLoginAttemptService}, which stores state in memory.
 * For clustered deployments, provide a custom bean backed by a distributed store (e.g. Redis).
 */
public interface LoginAttemptService
{
    /**
     * Records a failed login attempt for the given username.
     * When the number of consecutive failures reaches the configured maximum,
     * the account is temporarily locked.
     */
    void registerFailure(String username);

    /**
     * Clears the failure counter for the given username after a successful login.
     */
    void registerSuccess(String username);

    /**
     * Returns true if the given username is currently locked out due to too many failed attempts.
     */
    boolean isBlocked(String username);
}
