/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.eureka;

import com.netflix.discovery.DiscoveryClient;
import hu.perit.spvitamin.core.exception.ServerException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

@Configuration
public class EurekaClientSslConfig {

    @Bean
    public DiscoveryClient.DiscoveryClientOptionalArgs getTrustStoredEurekaClient() {
        DiscoveryClient.DiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        args.setSSLContext(this.getSslContext());
        args.setHostnameVerifier(new NoopHostnameVerifier());
        return args;
    }

    private SSLContext getSslContext() {
        // TrustSelfSignedStrategy
        try {
            return SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build();
        }
        catch (Exception ex) {
            return ServerException.throwFrom(ex);
        }
    }
}
