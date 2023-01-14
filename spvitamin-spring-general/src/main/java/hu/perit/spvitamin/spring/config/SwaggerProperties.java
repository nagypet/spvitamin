/*
 * Copyright 2020-2023 the original author or authors.
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

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "springfox.documentation")
@Slf4j
public class SwaggerProperties
{
    @NestedConfigurationProperty
    private SwaggerUiConfigurationProperties swaggerUi = new SwaggerUiConfigurationProperties();

    @NestedConfigurationProperty
    private SwaggerConfigurationProperties swagger = new SwaggerConfigurationProperties();

    @Setter
    public static class SwaggerUiConfigurationProperties
    {
        private String baseUrl;

        public String getBaseUrl()
        {
            String baseUrl = this.baseUrl;
            if (baseUrl == null)
            {
                baseUrl = "";
            }
            if (!baseUrl.isEmpty() && !baseUrl.startsWith("/"))
            {
                baseUrl = "/" + baseUrl;
            }
            return baseUrl;
        }
    }

    @Setter
    public static class SwaggerConfigurationProperties
    {
        @NestedConfigurationProperty
        private Swagger2Configuration v2 = new Swagger2Configuration();

        public Swagger2Configuration getV2()
        {
            return v2;
        }
    }

    @Setter
    public static class Swagger2Configuration
    {
        private String path;

        public String getPath()
        {
            String path = this.path;
            if (StringUtils.isBlank(path))
            {
                path = "/v2/api-docs";
            }
            if (!path.isEmpty() && !path.startsWith("/"))
            {
                path = "/" + path;
            }
            return path;
        }
    }
}
