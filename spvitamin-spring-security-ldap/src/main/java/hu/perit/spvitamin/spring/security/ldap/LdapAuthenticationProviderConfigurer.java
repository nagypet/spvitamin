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

package hu.perit.spvitamin.spring.security.ldap;

import hu.perit.spvitamin.spring.security.ldap.config.LdapCollectionProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class LdapAuthenticationProviderConfigurer
{
    public static final String LDAP_CONNECT_TIMEOUT_KEY = "com.sun.jndi.ldap.connect.timeout";
    private final LdapCollectionProperties ldapCollectionProperties;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;


    public void configure(HttpSecurity http)
    {
        for (Map.Entry<String, LdapCollectionProperties.LdapProperties> entry : this.ldapCollectionProperties.getLdaps().entrySet()) {

            LdapCollectionProperties.LdapProperties ldapProperties = entry.getValue();

            Map<String, Object> ctxEnvironmentProps = new HashMap<>();
            ctxEnvironmentProps.put(LDAP_CONNECT_TIMEOUT_KEY, String.valueOf(ldapProperties.getConnectTimeoutMs()));

            if (ldapProperties.isEnabled()) {
                LdapAuthenticationProvider provider = this.createProvider(
                        entry.getKey(),
                        ldapProperties.getUrl(),
                        ldapProperties.getDomain(),
                        ldapProperties.getFilter(),
                        ldapProperties.isUserprincipalWithDomain(),
                        ldapProperties.getRootDN(),
                        ldapProperties.getBindUserPattern());

                provider.setContextEnvironmentProperties(ctxEnvironmentProps);

                http.authenticationProvider(provider);
                this.authenticationManagerBuilder.authenticationProvider(provider);
            }
        }
    }


    private LdapAuthenticationProvider createProvider(String name, String url, String domain, String filter, Boolean withDomain, String rootDN, String bindUserPattern)
    {
        log.info(String.format("'%s' url: '%s', rootDN: '%s', filter: '%s', domain: '%s', with domain: '%b'", name, url, rootDN, filter, domain, withDomain));

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(domain, url, rootDN);
        provider.setSearchFilter(filter);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserprincipalwithdomain(withDomain);
        provider.setBindUserPattern(bindUserPattern);

        return provider;
    }
}
