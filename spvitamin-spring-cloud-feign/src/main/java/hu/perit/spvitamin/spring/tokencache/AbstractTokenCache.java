package hu.perit.spvitamin.spring.tokencache;

import feign.auth.BasicAuthRequestInterceptor;
import hu.perit.spvitamin.core.cache.InMemoryCache;
import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.CryptoProperties;
import hu.perit.spvitamin.spring.config.MicroserviceCollectionProperties;
import hu.perit.spvitamin.spring.config.MicroserviceProperties;
import hu.perit.spvitamin.spring.feignclients.SimpleFeignClientBuilder;
import hu.perit.spvitamin.spring.http.ResponseEntityUtils;
import hu.perit.spvitamin.spring.rest.api.AuthApi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public abstract class AbstractTokenCache<T extends Enum<T>> implements TokenCache<T>
{
    private final MicroserviceCollectionProperties microserviceCollectionProperties;
    private final CryptoProperties cryptoProperties;

    private final InMemoryCache<T, ArchiveSystemToken> cache = new InMemoryCache<>();
    private final Map<T, AuthApi> authApis = new HashMap<>();


    protected void setup(Map<T, String> microserviceProperties)
    {
        CryptoUtil cryptoUtil = new CryptoUtil();
        for (Map.Entry<T, String> propertiesEntry : microserviceProperties.entrySet())
        {
            MicroserviceProperties properties = this.microserviceCollectionProperties.get(propertiesEntry.getValue());
            String encrypted = properties.getAuth().getEncryptedPassword();
            String decrypted = StringUtils.isNotBlank(encrypted)
                    ? cryptoUtil.decrypt(this.cryptoProperties.getSecret(), encrypted)
                    : properties.getAuth().getPassword();
            Objects.requireNonNull(decrypted, "Password cannot be null!");
            this.authApis.put(propertiesEntry.getKey(), SimpleFeignClientBuilder.newInstance()
                    .browserModeWithClientId(propertiesEntry.getKey().name())
                    .requestInterceptor(new BasicAuthRequestInterceptor(properties.getAuth().getUsername(), decrypted))
                    .build(AuthApi.class, properties.getUrl()));
        }
    }


    @Override
    public AuthorizationToken getToken(T system)
    {
        ArchiveSystemToken token = this.cache.get(system);
        if (isValid(token))
        {
            return token.getToken();
        }
        return this.cache.update(system, () -> this.requestNewToken(system)).getToken();
    }


    @Override
    public void clearCachedToken(T system)
    {
        this.cache.remove(system);
    }


    private static boolean isValid(ArchiveSystemToken token)
    {
        if (token == null)
        {
            return false;
        }
        return token.isValid();
    }


    private ArchiveSystemToken requestNewToken(T microservice)
    {
        AuthApi authApi = this.authApis.get(microservice);
        if (authApi == null)
        {
            throw new IllegalStateException("Unexpected value: " + microservice);
        }
        AuthorizationToken token = ResponseEntityUtils.get(authApi.authenticateUsingGET(null));
        return new ArchiveSystemToken(token);
    }
}
