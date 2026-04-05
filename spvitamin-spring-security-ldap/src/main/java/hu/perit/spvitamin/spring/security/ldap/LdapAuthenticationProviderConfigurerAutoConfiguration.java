package hu.perit.spvitamin.spring.security.ldap;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class LdapAuthenticationProviderConfigurerAutoConfiguration
{
    @Bean
    public static LdapAuthenticationProviderConfigurer ldapAuthenticationProviderConfigurer()
    {
        return new LdapAuthenticationProviderConfigurer();
    }
}
