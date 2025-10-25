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
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties
@DependsOn(value = "SpvitaminSpringContext")
public class MicroserviceCollectionProperties
{
    @Getter(AccessLevel.NONE)
    private final SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);

    private Map<String, MicroserviceProperties> microservices = new HashMap<>();


    public MicroserviceProperties get(String name)
    {
        return this.microservices.get(name);
    }


    @PostConstruct
    private void init()
    {
        if (securityProperties.getMode() == SecurityProperties.Mode.RESOURCE_SERVER && !this.microservices.containsKey("auth-service"))
        {
            throw new IllegalStateException(MessageFormat.format("The 'auth-service' microservice is required for the {0} mode!", SecurityProperties.Mode.RESOURCE_SERVER));
        }
    }
}
