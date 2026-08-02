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
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link LoginAttemptService}.
 * <p>
 * Suitable for single-node deployments. For clustered environments, replace this bean
 * with a distributed implementation (e.g. Redis-backed) by declaring your own
 * {@code LoginAttemptService} bean — the auto-configuration uses {@code @ConditionalOnMissingBean}.
 * <p>
 * State is lost on application restart, which is acceptable: attackers lose any
 * progress toward a lockout, and legitimate users regain immediate access.
 */
@Slf4j
@RequiredArgsConstructor
public class InMemoryLoginAttemptService implements LoginAttemptService
{
    private record AttemptInfo(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    private final BruteForceProperties properties;


    @Override
    public void registerFailure(String username)
    {
        String key = username.toLowerCase();
        attempts.compute(key, (k, existing) -> {
            int newCount = (existing == null ? 0 : existing.count()) + 1;
            Instant lockedUntil = null;
            if (newCount >= properties.getMaxAttempts())
            {
                lockedUntil = Instant.now().plus(properties.getLockDuration());
                log.warn("Account locked due to too many failed login attempts: username={}, attempts={}, lockedUntil={}",
                        username, newCount, lockedUntil);
            }
            else
            {
                log.warn("Failed login attempt: username={}, failedAttempts={}/{}", username, newCount, properties.getMaxAttempts());
            }
            return new AttemptInfo(newCount, lockedUntil);
        });
    }


    @Override
    public void registerSuccess(String username)
    {
        attempts.remove(username.toLowerCase());
    }


    @Override
    public boolean isBlocked(String username)
    {
        AttemptInfo info = attempts.get(username.toLowerCase());
        if (info == null || info.lockedUntil() == null)
        {
            return false;
        }
        if (Instant.now().isAfter(info.lockedUntil()))
        {
            attempts.remove(username.toLowerCase());
            return false;
        }
        return true;
    }
}
