package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;

public interface AuthenticatedUserFactory
{
    boolean canHandle(Object principal);

    AuthenticatedUser createAuthenticatedUser(Object principal);
}
