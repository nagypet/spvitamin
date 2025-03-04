package hu.perit.spvitamin.spring.info;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class RequestInfoService
{
    private final HttpServletRequest httpServletRequest;


    public RequestInfo getRequestInfo()
    {
        String referer = httpServletRequest.getHeader("Referer");
        if (referer != null)
        {
            try
            {
                URL url = new URL(referer);
                return new RequestInfo(url.getProtocol(), url.getHost(), url.getPort());
            }
            catch (MalformedURLException e)
            {
                // Fallback if the referer is malformed
            }
        }

        // Using X-Forwarded-Proto and X-Forwarded-Host if there was no Referer
        String scheme = httpServletRequest.getHeader("X-Forwarded-Proto");
        String serverName = httpServletRequest.getHeader("X-Forwarded-Host");

        if (scheme == null)
        {
            scheme = httpServletRequest.getScheme();
        }
        if (serverName == null)
        {
            serverName = httpServletRequest.getServerName();
        }

        return new RequestInfo(scheme, serverName, httpServletRequest.getServerPort());
    }


    public String getBaseUrl()
    {
        RequestInfo requestInfo = getRequestInfo();

        if (requestInfo.getServerPort() < 0
                || (requestInfo.getScheme().equals("http") && requestInfo.getServerPort() == 80)
                || (requestInfo.getScheme().equals("https") && requestInfo.getServerPort() == 443)
        )
        {
            return requestInfo.getScheme() + "://" + requestInfo.getServerName();
        }

        return requestInfo.getScheme() + "://" + requestInfo.getServerName() + ":" + requestInfo.getServerPort();
    }

}
