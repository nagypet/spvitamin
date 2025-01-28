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

package hu.perit.spvitamin.spring.security;

import hu.perit.spvitamin.spring.config.ServerProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Provider;
import java.security.Security;

/**
 * #know-how:disable-ssl-certificate-validation
 *
 * @author Peter Nagy
 */

@Component
@Slf4j
public class NullSecurityProviderConfigurer
{

    private final ServerProperties serverProperties;


    public NullSecurityProviderConfigurer(ServerProperties serverProperties)
    {
        this.serverProperties = serverProperties;
    }


    @PostConstruct
    void init()
    {
        if (this.serverProperties.getSsl() != null && this.serverProperties.getSsl().isIgnoreCertificateValidation())
        {
            Provider nullSecurityProvider = new NullSecurityProvider("NullSecurityProvider", "1.0", "Skipping SSL certificate validation");
            Security.insertProviderAt(nullSecurityProvider, 1);

            log.warn("NullSecurityProvider installed!");
        }
    }
}
