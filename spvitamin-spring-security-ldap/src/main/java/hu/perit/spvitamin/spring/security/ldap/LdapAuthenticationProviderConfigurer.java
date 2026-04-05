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

import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import hu.perit.spvitamin.spring.security.ldap.config.LdapCollectionProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
public class LdapAuthenticationProviderConfigurer implements BeanDefinitionRegistryPostProcessor, PriorityOrdered
{
    public static final String LDAP_CONNECT_TIMEOUT_KEY = "com.sun.jndi.ldap.connect.timeout";


    @Override
    public int getOrder()
    {
        return Ordered.HIGHEST_PRECEDENCE;
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException
    {
        Environment environment = getEnvironment();
        if (environment == null)
        {
            log.warn("*** Failed to retrieve Spring environment, LDAP authentication provider configurations will not be processed.");
            return;
        }
        LdapCollectionProperties ldapCollectionProperties = LdapCollectionProperties.bindLdapCollectionProperties(environment);
        log.debug("Processing LDAP authentication provider configurations for {} providers", ldapCollectionProperties.getLdaps().size());
        for (Map.Entry<String, LdapCollectionProperties.LdapProperties> entry : ldapCollectionProperties.getLdaps().entrySet())
        {
            String id = entry.getKey();
            LdapCollectionProperties.LdapProperties props = entry.getValue();

            if (!props.isEnabled())
            {
                continue;
            }

            String beanName = "ldapAuthenticationProvider_" + toSafeBeanName(id);
            if (registry.containsBeanDefinition(beanName))
            {
                log.debug("LDAP provider bean definition '{}' already exists, skipping.", beanName);
                continue;
            }

            Map<String, Object> ctxEnvironmentProps = new HashMap<>();
            ctxEnvironmentProps.put(LDAP_CONNECT_TIMEOUT_KEY, String.valueOf(props.getConnectTimeoutMs()));

            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                    .genericBeanDefinition(LdapAuthenticationProvider.class, () ->
                    {
                        log.info(String.format("'%s' url: '%s', rootDN: '%s', filter: '%s', domain: '%s', with domain: '%b'",
                                id, props.getUrl(), props.getRootDN(), props.getFilter(), props.getDomain(), props.isUserprincipalWithDomain()));

                        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(
                                props.getDomain(),
                                props.getUrl(),
                                props.getRootDN()
                        );
                        provider.setSearchFilter(props.getFilter());
                        provider.setConvertSubErrorCodesToExceptions(true);
                        provider.setUseAuthenticationRequestCredentials(true);
                        provider.setUserprincipalwithdomain(props.isUserprincipalWithDomain());
                        provider.setBindUserPattern(props.getBindUserPattern());
                        provider.setEnableAccessWithoutDomain(props.isEnableAccessWithoutDomain());
                        provider.setContextEnvironmentProperties(ctxEnvironmentProps);

                        return provider;
                    });

            registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
            log.info("LDAP AuthenticationProvider bean definition has been registered: {}", beanName);
        }
    }


    private static Environment getEnvironment()
    {
        try
        {
            return SpringEnvironment.get();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    private static String toSafeBeanName(String name)
    {
        // Bean-névhez barátságos alak
        return name.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }


    private LdapAuthenticationProvider createProvider(String name, String url, String domain, String filter, Boolean withDomain, String rootDN, String bindUserPattern, boolean enableAccessWithoutDomain)
    {
        log.info(String.format("'%s' url: '%s', rootDN: '%s', filter: '%s', domain: '%s', with domain: '%b'", name, url, rootDN, filter, domain, withDomain));

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(domain, url, rootDN);
        provider.setSearchFilter(filter);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserprincipalwithdomain(withDomain);
        provider.setBindUserPattern(bindUserPattern);
        provider.setEnableAccessWithoutDomain(enableAccessWithoutDomain);

        return provider;
    }
}
