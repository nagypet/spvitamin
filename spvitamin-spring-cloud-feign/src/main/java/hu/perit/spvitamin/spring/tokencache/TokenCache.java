package hu.perit.spvitamin.spring.tokencache;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;

public interface TokenCache<T>
{
    AuthorizationToken getToken(T system);

    void clearCachedToken(T system);
}
