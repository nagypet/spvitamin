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

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
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

            // In Spring Boot 4.1.0+, SSLContext.getDefault() may already be cached before this @PostConstruct
            // runs due to earlier SSL initialization in the framework. We must explicitly override the default
            // SSLContext and the HttpsURLConnection default socket factory to ensure all HTTPS connections
            // use the null trust manager.
            try
            {
                SSLContext nullContext = SSLContext.getInstance("TLS");
                nullContext.init(null, new TrustManager[]{new NullTrustManager()}, null);
                SSLContext.setDefault(nullContext);
                HttpsURLConnection.setDefaultSSLSocketFactory(nullContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            }
            catch (Exception e)
            {
                log.error("Failed to override default SSL context", e);
            }

            log.warn("NullSecurityProvider installed!");
        }
    }
}
