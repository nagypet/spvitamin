package hu.perit.spvitamin.spring.email.imap.client;

import hu.perit.spvitamin.spring.email.imap.config.ImapConnectionConfig;
import hu.perit.spvitamin.spring.email.imap.config.ImapDownloaderProperties;
import hu.perit.spvitamin.spring.email.imap.auth.OAuth2TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Properties;

/**
 * Factory for creating {@link DefaultImapClient} instances from
 * {@link ImapDownloaderProperties} configuration.
 * <p>
 * Centralises connection-building (BASIC and OAuth2) so that multiple IMAP consumers
 * (invoice downloader, bounce mail processor, …) can reuse the same logic without
 * duplication.
 */
@Component
@RequiredArgsConstructor
public class ImapClientFactory
{
    static final int MAIL_CONNECT_TIMEOUT_MS = (int) Duration.ofSeconds(60).toMillis();
    static final int MAIL_READ_TIMEOUT_MS = (int) Duration.ofSeconds(90).toMillis();
    static final int MAIL_WRITE_TIMEOUT_MS = (int) Duration.ofSeconds(90).toMillis();

    private final OAuth2TokenProvider oAuth2TokenProvider;


    /**
     * Creates a connected {@link DefaultImapClient} for the given properties.
     * The caller is responsible for closing the client (try-with-resources recommended).
     */
    public DefaultImapClient createClient(ImapDownloaderProperties props)
    {
        return new DefaultImapClient(buildConfig(props));
    }


    /**
     * Builds an {@link ImapConnectionConfig} from the given properties.
     * Handles both BASIC and OAUTH2 auth types.
     */
    public ImapConnectionConfig buildConfig(ImapDownloaderProperties props)
    {
        return switch (props.getAuthType())
        {
            case BASIC -> buildBasicConfig(props);
            case OAUTH2 -> buildOAuth2Config(props);
        };
    }


    private static ImapConnectionConfig buildBasicConfig(ImapDownloaderProperties props)
    {
        return props.toConnectionConfig().toBuilder()
                .additionalProperties(buildTimeoutProperties())
                .build();
    }


    private ImapConnectionConfig buildOAuth2Config(ImapDownloaderProperties props)
    {
        String accessToken = this.oAuth2TokenProvider.acquireAccessToken(props.getOauth2());

        Properties additional = buildTimeoutProperties();
        additional.setProperty("mail.imap.sasl.enable", "true");
        additional.setProperty("mail.imap.sasl.mechanisms", "XOAUTH2");
        additional.setProperty("mail.imap.auth.mechanisms", "XOAUTH2");
        additional.setProperty("mail.imap.auth.login.disable", "true");
        additional.setProperty("mail.imap.auth.plain.disable", "true");
        if (props.isUseSSL())
        {
            additional.setProperty("mail.imap.ssl.enable", "true");
        }

        return props.toConnectionConfig().toBuilder()
                .password(accessToken)
                .additionalProperties(additional)
                .build();
    }


    private static Properties buildTimeoutProperties()
    {
        Properties props = new Properties();
        props.setProperty("mail.imap.connectiontimeout", String.valueOf(MAIL_CONNECT_TIMEOUT_MS));
        props.setProperty("mail.imap.timeout", String.valueOf(MAIL_READ_TIMEOUT_MS));
        props.setProperty("mail.imap.writetimeout", String.valueOf(MAIL_WRITE_TIMEOUT_MS));
        props.setProperty("mail.imaps.connectiontimeout", String.valueOf(MAIL_CONNECT_TIMEOUT_MS));
        props.setProperty("mail.imaps.timeout", String.valueOf(MAIL_READ_TIMEOUT_MS));
        props.setProperty("mail.imaps.writetimeout", String.valueOf(MAIL_WRITE_TIMEOUT_MS));
        return props;
    }
}
