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

package hu.perit.spvitamin.test.spring.config;

import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringContextMock
{
    public static Builder builder()
    {
        return new Builder();
    }

    private static void init(ApplicationContext applicationContext, Map<Class<?>, Object> beanMap)
    {
        lenient().when(applicationContext.getBean((Class<?>) any(Class.class))).thenAnswer(invocation -> {
            Class<?> requestedBean = invocation.getArgument(0);
            if (beanMap.containsKey(requestedBean))
            {
                return beanMap.get(requestedBean);
            }
            else
            {
                throw new RuntimeException("Type is not mocked: " + requestedBean.getSimpleName());
            }
        });

        ReflectionTestUtils.setField(SpringContext.class, "context", applicationContext);
    }

    public static class Builder
    {
        private final Map<Class<?>, Object> beanMap = new HashMap<>();

        public Builder append(Class<?> clazz, Object bean)
        {
            beanMap.put(clazz, bean);
            return this;
        }

        public void build(ApplicationContext applicationContext)
        {
            init(applicationContext, beanMap);
        }
    }
}
