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

package hu.perit.spvitamin.spring.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * @author Peter Nagy
 */


@Data
@Component
@ConfigurationProperties("server")
public class ServerProperties {

    private String fqdn = "localhost";
    private int port = 8080;

    @NestedConfigurationProperty
    private Ssl ssl;

    public String getProtocollAsString() {
        if (this.ssl == null) return "http";
        return this.ssl.enabled ? "https" : "http";
    }

    public String getServiceUrl() {
        return String.format("%s://%s:%d", this.getProtocollAsString(), this.fqdn, this.port);
    }

    @Getter
    @Setter
    public static class Ssl {
        private boolean enabled = false;
        private boolean ignoreCertificateValidation = false;
        private String keyStore;
        @ConfigProperty(hidden = true)
        private String keyStorePassword;
        private String keyAlias;
        private String trustStore;
        @ConfigProperty(hidden = true)
        private String trustStorePassword;
    }
}
