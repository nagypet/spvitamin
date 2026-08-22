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

package hu.perit.spvitamin.spring.session.redis;

import hu.perit.spvitamin.spring.security.bruteforce.BruteForceProperties;
import hu.perit.spvitamin.spring.security.bruteforce.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Redis-backed implementation of {@link LoginAttemptService}.
 * <p>
 * Suitable for multi-pod (clustered) deployments: all application instances share
 * the same counters and lockout state through Redis.
 * <p>
 * Redis key schema:
 * <ul>
 *   <li>{@code brute-force:<username>:count} — consecutive failure counter (INCR)</li>
 *   <li>{@code brute-force:<username>:locked} — present = locked; TTL = lockDuration</li>
 * </ul>
 * Both keys are automatically removed by Redis TTL, so no manual cleanup is needed.
 * <p>
 * This bean is registered automatically by {@link RedisBruteForceAutoConfiguration}
 * and takes precedence over the in-memory fallback when Redis is available.
 */
@Slf4j
@RequiredArgsConstructor
public class RedisLoginAttemptService implements LoginAttemptService
{
    static final String KEY_PREFIX = "brute-force:";
    static final String COUNT_SUFFIX = ":count";
    static final String LOCK_SUFFIX = ":locked";

    private final StringRedisTemplate redisTemplate;
    private final BruteForceProperties properties;


    @Override
    public void registerFailure(String username)
    {
        try
        {
            String countKey = countKey(username);
            String lockKey = lockKey(username);

            Long count = redisTemplate.opsForValue().increment(countKey);
            // Set a cleanup TTL on the counter so it does not accumulate in Redis indefinitely
            redisTemplate.expire(countKey, properties.getLockDuration().plusHours(1));

            if (count != null && count >= properties.getMaxAttempts())
            {
                redisTemplate.opsForValue().set(lockKey, "1", properties.getLockDuration());
                log.warn("Account locked due to too many failed login attempts: username={}, attempts={}, lockDuration={}",
                        username, count, properties.getLockDuration());
            }
            else
            {
                log.warn("Failed login attempt: username={}, failedAttempts={}/{}", username, count, properties.getMaxAttempts());
            }
        }
        catch (Exception e)
        {
            log.error("Redis unavailable while registering failed login attempt for user '{}': {}", username, e.getMessage());
        }
    }


    @Override
    public void registerSuccess(String username)
    {
        try
        {
            redisTemplate.delete(List.of(countKey(username), lockKey(username)));
        }
        catch (Exception e)
        {
            log.error("Redis unavailable while clearing brute-force counters for user '{}': {}", username, e.getMessage());
        }
    }


    @Override
    public boolean isBlocked(String username)
    {
        try
        {
            return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey(username)));
        }
        catch (Exception e)
        {
            log.error("Redis unavailable while checking brute-force lock for user '{}', failing open: {}", username, e.getMessage());
            return false;
        }
    }


    private String countKey(String username)
    {
        return KEY_PREFIX + username.toLowerCase() + COUNT_SUFFIX;
    }


    private String lockKey(String username)
    {
        return KEY_PREFIX + username.toLowerCase() + LOCK_SUFFIX;
    }
}
