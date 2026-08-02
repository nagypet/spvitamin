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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties("security.brute-force")
public class BruteForceProperties
{
    /**
     * Enables brute force protection. Default: true.
     */
    private boolean enabled = true;

    /**
     * Number of consecutive failed login attempts before the account is temporarily locked. Default: 5.
     */
    private int maxAttempts = 5;

    /**
     * How long the account remains locked after exceeding maxAttempts. Default: 15 minutes.
     * Use ISO-8601 duration format, e.g. PT15M.
     */
    private Duration lockDuration = Duration.ofMinutes(15);
}
