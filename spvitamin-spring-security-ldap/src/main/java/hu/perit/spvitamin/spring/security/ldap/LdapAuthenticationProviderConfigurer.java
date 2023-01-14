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

package hu.perit.spvitamin.spring.security.ldap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Component;

import hu.perit.spvitamin.spring.security.ldap.config.LdapCollectionProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Component
public class LdapAuthenticationProviderConfigurer
{
    public static final String LDAP_CONNECT_TIMEOUT_KEY = "com.sun.jndi.ldap.connect.timeout";
    private final LdapCollectionProperties ldapCollectionProperties;

    public LdapAuthenticationProviderConfigurer(LdapCollectionProperties ldapCollectionProperties) {
        this.ldapCollectionProperties = ldapCollectionProperties;
    }

    public void configure(AuthenticationManagerBuilder auth)
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

                auth.authenticationProvider(provider);
            }
        }
    }


    private LdapAuthenticationProvider createProvider(String name, String url, String domain, String filter, Boolean withDomain, String rootDN, String bindUserPattern)
    {
        log.debug(String.format("'%s' url: '%s', rootDN: '%s', filter: '%s', domain: '%s', with domain: '%b'", name, url, rootDN, filter, domain, withDomain));

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(domain, url, rootDN);
        provider.setSearchFilter(filter);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserprincipalwithdomain(withDomain);
        provider.setBindUserPattern(bindUserPattern);

        return provider;
    }
}
