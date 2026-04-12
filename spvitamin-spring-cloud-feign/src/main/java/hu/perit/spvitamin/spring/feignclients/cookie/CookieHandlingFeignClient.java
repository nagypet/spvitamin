package hu.perit.spvitamin.spring.feignclients.cookie;

import feign.Client;
import feign.Request;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;

@RequiredArgsConstructor
public class CookieHandlingFeignClient implements Client
{
    private final Client delegate;
    private final CookieStoreService cookieStoreService;
    private final String clientId;


    @Override
    public Response execute(Request request, Request.Options options) throws IOException
    {
        Response response = delegate.execute(request, options);
        storeCookies(response);
        return response;
    }


    private void storeCookies(Response response)
    {
        Collection<String> setCookies = response.headers().get("set-cookie");
        if (setCookies == null || setCookies.isEmpty())
        {
            setCookies = response.headers().get("Set-Cookie");
        }
        if (setCookies == null || setCookies.isEmpty())
        {
            return;
        }

        CookieJar cookieJar = this.cookieStoreService.getCookieJar(this.clientId);
        if (cookieJar == null)
        {
            cookieJar = new CookieJar();
        }

        for (String header : setCookies)
        {
            CookiePair cookiePair = parseCookie(header);
            if (cookiePair != null)
            {
                cookieJar.getCookies().put(cookiePair.name(), cookiePair.value());
            }
        }

        this.cookieStoreService.putCookieJar(this.clientId, cookieJar);
    }


    private CookiePair parseCookie(String setCookieHeader)
    {
        if (StringUtils.isBlank(setCookieHeader))
        {
            return null;
        }

        String firstPart = setCookieHeader.split(";", 2)[0];
        int idx = firstPart.indexOf('=');
        if (idx <= 0)
        {
            return null;
        }

        String name = firstPart.substring(0, idx).trim();
        String value = firstPart.substring(idx + 1).trim();
        if (StringUtils.isBlank(name))
        {
            return null;
        }

        return new CookiePair(name, value);
    }


    private record CookiePair(String name, String value)
    {
    }
}
