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

package hu.perit.spvitamin.spring.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties(prefix = "spvitamin.session")
@Slf4j
public class SessionProperties
{
    private Duration sessionMaxAge = Duration.ofDays(30);

    private String cookieName = "SESSION";

    @NestedConfigurationProperty
    private Limits limits = new Limits();


    @Getter
    @Setter
    @ToString
    public static class Limits
    {
        private int defaultLimit = -1;
        private Map<String, Integer> byRole = new HashMap<>();
    }


    @PostConstruct
    private void postConstruct()
    {
        log.info(this.toString());
    }


    public int getSessionLimit(Collection<String> roles)
    {
        int defaultLimit = this.limits != null ? this.limits.getDefaultLimit() : -1;
        Map<String, Integer> byRole = (this.limits != null && this.limits.getByRole() != null)
                ? this.limits.getByRole()
                : java.util.Collections.emptyMap();

        if (roles == null || roles.isEmpty())
        {
            return defaultLimit;
        }

        Integer mostRestrictive = null; // legkisebb nem negatív érték
        boolean hasUnlimitedForAnyRole = false; // van-e negatív (pl. -1) bármely szerepre

        for (String role : roles)
        {
            if (role == null)
            {
                continue;
            }
            Integer value = byRole.get(role);
            if (value == null)
            {
                continue;
            }

            if (value >= 0)
            {
                if (mostRestrictive == null || value < mostRestrictive)
                {
                    mostRestrictive = value;
                }
            }
            else
            {
                hasUnlimitedForAnyRole = true;
            }
        }

        if (mostRestrictive != null)
        {
            return mostRestrictive;
        }

        if (hasUnlimitedForAnyRole)
        {
            return -1; // korlátlan
        }

        return defaultLimit;
    }
}
