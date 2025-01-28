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

package hu.perit.spvitamin.spring.security.ldap.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties
public class LdapCollectionProperties {

    private Map<String, LdapProperties> ldaps = new HashMap<>();

    @Getter
    @Setter
    public static class LdapProperties {

        private boolean enabled = true;
        private String url;
        private String rootDN;
        private String filter = "(&(objectClass=user)(sAMAccountName={0}))";
        private boolean userprincipalWithDomain = false;
        private String domain;
        private int connectTimeoutMs = 1000;
        private String bindUserPattern;
    }
}
