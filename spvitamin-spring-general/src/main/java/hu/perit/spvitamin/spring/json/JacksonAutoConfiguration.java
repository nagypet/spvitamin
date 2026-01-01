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

package hu.perit.spvitamin.spring.json;

import hu.perit.spvitamin.json.SpvitaminObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;


/**
 * Extends JacksonAutoConfiguration with custom serializers and deserializers
 */

@Configuration
public class JacksonAutoConfiguration
{
    @Bean
    @Primary
    public JsonMapper jsonMapper()
    {
        // Register additional modules for use within the Spring framework
        SpvitaminObjectMapper.addModule(new SpvitaminJsonSpringModule());

        return SpvitaminObjectMapper.getJsonMapper();
    }
}
