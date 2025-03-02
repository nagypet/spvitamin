package hu.perit.spvitamin.spring.security.config;

public class BasicAuthType extends AuthenticationType
{
    public static final String TYPE = "basic";
    public static final String LABEL = "Username/Password";

    public BasicAuthType()
    {
        super(TYPE, LABEL, null);
    }
}
