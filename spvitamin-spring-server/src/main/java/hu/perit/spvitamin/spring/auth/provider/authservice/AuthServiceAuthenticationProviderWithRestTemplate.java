/*
 * Copyright 2020-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.auth.provider.authservice;

import hu.perit.spvitamin.spring.auth.BasicAuthHeader;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.config.SysMicroservices;
import hu.perit.spvitamin.spring.rest.model.AuthorizationToken;
import lombok.extern.log4j.Log4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j
public class AuthServiceAuthenticationProviderWithRestTemplate extends AuthServiceAuthenticationProvider {

    private final RestTemplate restTemplate;

    public AuthServiceAuthenticationProviderWithRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected AuthorizationToken getAuthorizationToken(String userName, String password) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, BasicAuthHeader.fromUsernamePassword(userName, password));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.ACCEPT, "application/json");

        // create request
        HttpEntity request = new HttpEntity(headers);

        ResponseEntity<AuthorizationToken> authorizationToken = this.restTemplate.exchange(getServiceUrl() + "/authenticate",
                HttpMethod.GET,
                request,
                AuthorizationToken.class);
        return authorizationToken.getBody();
    }


    public static String getServiceUrl() {
        SysMicroservices sysMicroservices = SysConfig.getSysMicroservices();
        return sysMicroservices.getAuthService().getUrl();
    }
}
