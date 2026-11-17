package hu.perit.spvitamin.spring.email.imap.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.NonNull;

import java.time.Duration;

//------------------------------------------------------------------------------------------------------------------
// ImapDownloaderProperties
//------------------------------------------------------------------------------------------------------------------
@Getter
@Setter
@ToString
public class ImapDownloaderProperties
{
    public enum AuthType
    {
        BASIC,
        OAUTH2
    }

    private boolean enabled = true;
    @NotNull
    private Duration pollInterval;
    /**
     * Maximum number of emails processed per run. Prevents timeout when the inbox contains
     * a large backlog (e.g. thousands of bounce emails). Remaining emails are picked up in
     * subsequent runs. 0 means unlimited (process all). Default: 100.
     */
    private int maxMessagesPerRun = 100;
    /**
     * Maximum total size of eligible messages fetched per run in MB.
     * Prevents memory exhaustion when many messages are close to the per-message size limit
     * (e.g. 100 messages × 9 MB = 900 MB). Messages deferred due to this limit are picked up
     * in subsequent runs. 0 means unlimited. Default: 100.
     */
    private int maxBatchSizeMb = 100;
    private AuthType authType = AuthType.BASIC;
    @NotNull
    private String host;
    @NotNull
    private String username;
    @NotNull
    private String password;
    private int port = 993;
    private boolean useSSL = true;
    @NotNull
    private String folder;
    private int maxMessageSizeMb = 10;

    @NestedConfigurationProperty
    private O365OAuth2Properties oauth2;


    @NonNull
    public ImapConnectionConfig toConnectionConfig()
    {
        return ImapConnectionConfig.builder()
                .host(this.host)
                .port(this.port)
                .username(this.username)
                .password(this.password)
                .useSSL(this.useSSL)
                .build();
    }
}
