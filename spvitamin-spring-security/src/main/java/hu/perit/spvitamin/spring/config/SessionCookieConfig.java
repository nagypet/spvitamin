package hu.perit.spvitamin.spring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@RequiredArgsConstructor
public class SessionCookieConfig
{
    private final JwtProperties jwtProperties;

    @Bean
    public CookieSerializer cookieSerializer()
    {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setSameSite("Lax");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true);
        serializer.setCookieMaxAge((int) this.jwtProperties.getRefreshExpiration().getSeconds());
        return serializer;
    }
}
