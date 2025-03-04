package hu.perit.spvitamin.spring.info;

import lombok.Data;

@Data
public class RequestInfo
{
    private final String scheme;
    private final String serverName;
    private final int serverPort;

    public String getBaseUrl()
    {
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
        {
            return scheme + "://" + serverName;
        }

        return scheme + "://" + serverName + ":" + serverPort;
    }
}
