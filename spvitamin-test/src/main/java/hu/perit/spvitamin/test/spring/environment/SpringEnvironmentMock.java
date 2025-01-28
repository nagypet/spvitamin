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

package hu.perit.spvitamin.test.spring.environment;

import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringEnvironmentMock
{
    public static Builder builder()
    {
        return new Builder();
    }

    private static void init(Environment environment, Map<String, String> propertyMap)
    {
        lenient().when(environment.getProperty(anyString())).thenAnswer(invocation -> {
            String requestedKey = invocation.getArgument(0);
            if (propertyMap.containsKey(requestedKey))
            {
                return propertyMap.get(requestedKey);
            }
            else
            {
                throw new RuntimeException("Key not found in mocked environment: " + requestedKey);
            }
        });

        SpringEnvironment.setEnvironment(environment);
    }

    public static class Builder
    {
        private final Map<String, String> propertyMap = new HashMap<>();

        public Builder append(String key, String value)
        {
            propertyMap.put(key, value);
            return this;
        }

        public void build(Environment environment)
        {
            init(environment, propertyMap);
        }
    }
}
