package hu.perit.spvitamin.spring.feignclients.cookie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieStoreService
{
    public static final String CACHE_NAME = "feign-cookie-jar";


    @Cacheable(cacheNames = CACHE_NAME, key = "#clientId", unless = "#result == null")
    public CookieJar getCookieJar(String clientId)
    {
        return new CookieJar();
    }


    @CachePut(cacheNames = CACHE_NAME, key = "#clientId")
    public CookieJar putCookieJar(String clientId, CookieJar cookieJar)
    {
        log.debug("putCookieJar [{}]: {}", clientId, cookieJar);
        return cookieJar;
    }


    @CacheEvict(cacheNames = CACHE_NAME, key = "#clientId")
    public void clear(String clientId)
    {
        // intentionally empty
    }
}
