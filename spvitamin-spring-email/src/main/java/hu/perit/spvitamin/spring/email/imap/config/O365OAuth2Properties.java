package hu.perit.spvitamin.spring.email.imap.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class O365OAuth2Properties
{
    @NotNull
    private String tenantId;
    @NotNull
    private String clientId;
    @NotNull
    private String clientSecret;
}
