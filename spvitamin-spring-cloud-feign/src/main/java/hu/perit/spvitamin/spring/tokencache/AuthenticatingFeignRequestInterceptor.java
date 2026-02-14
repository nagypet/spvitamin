package hu.perit.spvitamin.spring.tokencache;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.SpringContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthenticatingFeignRequestInterceptor<T> implements RequestInterceptor
{
    private final T system;


    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        TokenCache<T> tokenCache = SpringContext.getBean(TokenCache.class);
        AuthorizationToken token = tokenCache.getToken(system);
        requestTemplate.header("Authorization", "Bearer " + token.getJwt());
    }
}
