package hu.perit.spvitamin.spring.feignclients.cookie;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CookieRequestInterceptor implements RequestInterceptor
{
    private final CookieStoreService cookieStoreService;
    private final String clientId;


    @Override
    public void apply(RequestTemplate template)
    {
        CookieJar cookieJar = this.cookieStoreService.getCookieJar(this.clientId);
        if (cookieJar == null || cookieJar.getCookies() == null || cookieJar.getCookies().isEmpty())
        {
            return;
        }

        String cookieHeader = cookieJar.getCookies().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.joining("; "));

        if (StringUtils.isNotBlank(cookieHeader))
        {
            template.header("Cookie", cookieHeader);
        }
    }
}
