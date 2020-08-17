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

/*
 * Copyright (c) 2019. Innodox Technologies Zrt.
 * All rights reserved.
 */

package hu.perit.spvitamin.spring.security.ldap;

import hu.perit.spvitamin.spring.config.LdapProperties;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Log4j
@Getter
@Component
public class LdapAuthenticationProviderConfigurer
{
    private final LdapProperties ldapProperties;

    public LdapAuthenticationProviderConfigurer(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    public void configure(AuthenticationManagerBuilder auth)
    {
        for (Map.Entry<String, LdapProperties.SingleLdapProperties> entry : this.ldapProperties.getLdaps().entrySet()) {

            LdapProperties.SingleLdapProperties singleLdapProperties = entry.getValue();

            if (singleLdapProperties.isEnabled()) {
                LdapAuthenticationProvider provider = this.createProvider(
                        entry.getKey(),
                        singleLdapProperties.getUrl(),
                        singleLdapProperties.getDomain(),
                        singleLdapProperties.getFilter(),
                        singleLdapProperties.isUserprincipalWithDomain(),
                        singleLdapProperties.getRootDN());

                auth.authenticationProvider(provider);
            }
        }
    }


    private LdapAuthenticationProvider createProvider(String name, String url, String domain, String filter, Boolean withDomain, String rootDN)
    {
        log.debug(String.format("'%s' url: '%s', rootDN: '%s', filter: '%s', domain: '%s', with domain: '%b'", name, url, rootDN, filter, domain, withDomain));

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(domain, url, rootDN);
        provider.setSearchFilter(filter);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserprincipalwithdomain(withDomain);

        return provider;
    }
}
