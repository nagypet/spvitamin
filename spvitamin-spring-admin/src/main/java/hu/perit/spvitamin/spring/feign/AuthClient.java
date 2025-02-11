package hu.perit.spvitamin.spring.feign;

import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;


public interface AuthClient
{
    String BASE_URL_AUTHENTICATE = "/api/spvitamin/authenticate";

    @GetMapping(BASE_URL_AUTHENTICATE)
    AuthorizationToken authenticate(@RequestHeader String traceId);
}
