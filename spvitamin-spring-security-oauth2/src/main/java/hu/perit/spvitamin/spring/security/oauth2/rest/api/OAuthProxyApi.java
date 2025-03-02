package hu.perit.spvitamin.spring.security.oauth2.rest.api;

import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

public interface OAuthProxyApi
{
    String BASE_URL = "/api/spvitamin/oauth2/authorization";

    @GetMapping(BASE_URL)
    public void authorization(@RequestParam("provider") String provider) throws ResourceNotFoundException, IOException;
}
