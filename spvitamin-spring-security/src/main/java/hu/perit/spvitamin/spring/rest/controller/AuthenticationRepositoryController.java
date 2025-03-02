package hu.perit.spvitamin.spring.rest.controller;

import hu.perit.spvitamin.spring.rest.api.AuthenticationRepositoryApi;
import hu.perit.spvitamin.spring.security.config.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationRepositoryController implements AuthenticationRepositoryApi
{
    private final AuthenticationRepository authenticationRepository;

    @Override
    public AuthenticationRepository getAuthenticationRepository()
    {
        return this.authenticationRepository;
    }
}
