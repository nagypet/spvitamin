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

package hu.perit.spvitamin.spring.security.autoconfiguration;

import hu.perit.spvitamin.spring.config.SecurityProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.session.registry.AdvancedSessionRegistry;
import hu.perit.spvitamin.spring.session.strategy.PerUserTypeConcurrentSessionControlStrategy;
import hu.perit.spvitamin.spring.session.strategy.SpvitaminCompositeSessionAuthenticationStrategy;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.util.List;

@AutoConfiguration
@Slf4j
@DependsOn("SpvitaminSpringContext")
@RequiredArgsConstructor
public class SpvitaminSpringSecurityAutoconfiguration
{
    @Getter(AccessLevel.NONE)
    private final SecurityProperties securityProperties = SpringContext.getBean(SecurityProperties.class);


    @PostConstruct
    void init()
    {
        if (this.securityProperties.isProductionMode()
                && ("*".equals(this.securityProperties.getAdminEndpointsAccess())
                || "*".equals(this.securityProperties.getSwaggerAccess())
                || "*".equals(this.securityProperties.getManagementEndpointsAccess()))
        )
        {
            throw new IllegalStateException("Production mode is enabled, but either adminGuiAccess, adminEndpointsAccess, swaggerAccess or managementEndpointsAccess is set to '*'!");
        }
    }


    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(AdvancedSessionRegistry sessionRegistry)
    {
        var concurrent = new PerUserTypeConcurrentSessionControlStrategy(sessionRegistry);
        var register = new RegisterSessionAuthenticationStrategy(sessionRegistry);
        return new SpvitaminCompositeSessionAuthenticationStrategy(List.of(concurrent, register));
    }
}
