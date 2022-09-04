package hu.perit.spvitamin.spring.jobexecutor;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;

import java.util.concurrent.Callable;

public interface CancelableJobWithSecurityContext extends Callable<Boolean>
{
    AuthenticatedUser getAuthenticatedUser();

    void setAuthenticatedUser(AuthenticatedUser authenticatedUser);
}
