package hu.perit.spvitamin.spring.email.imap.auth;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import hu.perit.spvitamin.spring.email.imap.config.O365OAuth2Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Set;

@Service
@Slf4j
public class OAuth2TokenProvider
{
    // MSAL application instance should be reused to benefit from token caching
    private ConfidentialClientApplication confidentialClientApplication;


    public synchronized String acquireAccessToken(O365OAuth2Properties oauth2)
    {
        // Authority and scope for Outlook IMAP access
        final String authority = "https://login.microsoftonline.com/" + oauth2.getTenantId();
        final Set<String> scopes = Set.of("https://outlook.office365.com/.default");

        try
        {
            if (this.confidentialClientApplication == null)
            {
                this.confidentialClientApplication = ConfidentialClientApplication.builder(
                                oauth2.getClientId(),
                                ClientCredentialFactory.createFromSecret(oauth2.getClientSecret()))
                        .authority(authority)
                        .build();
            }

            ClientCredentialParameters parameters = ClientCredentialParameters.builder(scopes)
                    .build();

            // acquireToken calls are blocking network calls but MSAL handles caching internally.
            // It will return a cached token if it's not expired.
            IAuthenticationResult result = this.confidentialClientApplication.acquireToken(parameters).join();
            return result.accessToken();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException("Invalid authority URL for MSAL: " + authority, e);
        }
        catch (RuntimeException e)
        {
            // Let DefaultImapClient throw a meaningful error later; here we propagate
            throw e;
        }
    }
}
