package hu.perit.spvitamin.spring.security.config;

import lombok.Data;

@Data
public class AuthenticationType
{
    private final String type;
    private final String label;
    private final String provider;
}
