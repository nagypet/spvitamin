package hu.perit.spvitamin.spring.security;

public enum CredentialType
{
    UNKNOWN,
    ANONYMOUS,
    BASIC,
    BEARER,
    AT_IN_COOKIE,
    RT_IN_COOKIE,
    API_KEY,
    OAUTH2
}
