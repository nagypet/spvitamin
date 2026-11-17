package hu.perit.spvitamin.spring.email.imap.config;

import lombok.Builder;
import lombok.Data;

import java.util.Properties;

@Data
@Builder(toBuilder = true)
public class ImapConnectionConfig
{
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean useSSL;
    private Properties additionalProperties;
}
