package hu.perit.spvitamin.core.typehelpers;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

@UtilityClass
@Slf4j
public class UrlUtils
{
    /**
     * Extracts the domain from a URL.
     * This method returns the protocol and host part of the URL, removing "www." prefix if present.
     *
     * @param urlString The URL to extract the domain from
     * @return The domain of the URL in the format "protocol://host"
     */
    public String extractDomain(String urlString)
    {
        try
        {
            URI uri = new URI(urlString);
            String host = uri.getHost();
            if (host == null)
            {
                return null;
            }
            String hostWithoutWww = host.startsWith("www.") ? host.substring(4) : host;
            return String.format("%s://%s", uri.getScheme(), hostWithoutWww);
        }
        catch (URISyntaxException e)
        {
            log.warn("*** Failed to extract domain from URL {}: {}", urlString, e.getMessage());
            return null;
        }
    }

}
