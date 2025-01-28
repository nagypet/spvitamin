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

package hu.perit.spvitamin.spring.eureka.sslconfig;

import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.configuration.SSLContextFactory;
import org.springframework.cloud.configuration.TlsProperties;
import org.springframework.cloud.netflix.eureka.http.EurekaClientHttpRequestFactorySupplier;
import org.springframework.cloud.netflix.eureka.http.RestTemplateDiscoveryClientOptionalArgs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EurekaClientSslConfig
{
    /*
    @Bean
    public AbstractDiscoveryClientOptionalArgs<?> getDiscoveryClientOptionalArgs()
    {
        DiscoveryClient.DiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        args.setSSLContext(this.getSslContext());
        args.setHostnameVerifier(new NoopHostnameVerifier());
        //args.setEurekaJerseyClient(new EurekaJerseyClientImpl(2000, 5000, ));
        return args;
    }

    private SSLContext getSslContext()
    {
        // TrustSelfSignedStrategy
        try
        {
            return SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build();
        }
        catch (Exception ex)
        {
            return ServerException.throwFrom(ex);
        }
    }

     */


    @Bean
    public RestTemplateDiscoveryClientOptionalArgs restTemplateDiscoveryClientOptionalArgs(TlsProperties tlsProperties,
                                                                                           EurekaClientHttpRequestFactorySupplier eurekaClientHttpRequestFactorySupplier)
            throws GeneralSecurityException, IOException
    {
        log.info("Eureka HTTP Client uses RestTemplate.");
        RestTemplateDiscoveryClientOptionalArgs result = new RestTemplateDiscoveryClientOptionalArgs(
                eurekaClientHttpRequestFactorySupplier);
        setupTLS(result, tlsProperties);
        return result;
    }


    private static void setupTLS(AbstractDiscoveryClientOptionalArgs<?> args, TlsProperties properties)
            throws GeneralSecurityException, IOException
    {
        if (properties.isEnabled())
        {
            SSLContextFactory factory = new SSLContextFactory(properties);
            args.setSSLContext(factory.createSSLContext());
        }
    }
}
