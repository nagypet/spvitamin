package hu.perit.spvitamin.spring.rolemapper;

import hu.perit.spvitamin.spring.security.AuthenticatedUser;

public interface RoleMapperService
{
    boolean userHasRole(String username, String role);

    AuthenticatedUser mapGrantedAuthorities(AuthenticatedUser authenticatedUser);
}
