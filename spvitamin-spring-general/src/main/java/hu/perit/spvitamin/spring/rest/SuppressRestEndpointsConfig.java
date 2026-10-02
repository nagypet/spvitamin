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

package hu.perit.spvitamin.spring.rest;

import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * Configures a custom {@link RequestMappingHandlerMapping} that suppresses endpoint methods
 * declared in classes annotated with {@link SuppressRestEndpoints}.
 * <p>
 * Only methods declared directly in the concrete controller class (i.e. overridden methods)
 * are registered as REST endpoints. Methods that are merely inherited from a base class
 * annotated with {@link SuppressRestEndpoints} are skipped — both in Spring MVC and in
 * the OpenAPI/Swagger documentation.
 */
@Configuration
public class SuppressRestEndpointsConfig implements WebMvcRegistrations
{
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping()
    {
        return new RequestMappingHandlerMapping()
        {
            @Override
            protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType)
            {
                if (method.getDeclaringClass().isAnnotationPresent(SuppressRestEndpoints.class))
                {
                    return null;
                }
                return super.getMappingForMethod(method, handlerType);
            }
        };
    }
}
