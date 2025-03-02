package hu.perit.spvitamin.spring.security.oauth2;

import hu.perit.spvitamin.spring.security.config.AuthenticationType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class OAuth2AuthType extends AuthenticationType
{
    public static final String TYPE = "oauth2";

    public OAuth2AuthType(String provider, String displayName)
    {
        super(TYPE, displayName, provider);
    }
}
