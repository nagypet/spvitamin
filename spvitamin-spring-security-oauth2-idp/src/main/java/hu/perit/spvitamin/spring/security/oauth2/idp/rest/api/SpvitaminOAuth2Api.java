/*
 * Copyright 2020-2025 the original author or authors.
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

package hu.perit.spvitamin.spring.security.oauth2.idp.rest.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface SpvitaminOAuth2Api
{
    @PostMapping(path = "/api/spvitamin/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> token(
            @RequestParam MultiValueMap<String, String> form
    );


    @PostMapping(path = "/api/spvitamin/oauth2/refresh", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> refresh(
            @RequestParam MultiValueMap<String, String> form
    );


    @GetMapping(path = "/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> openidConfiguration();


    @GetMapping(path = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> jwks() throws Exception;


    @Secured({"SCOPE_OPENID", "SCOPE_PROFILE"})
    @GetMapping(path = "/api/spvitamin/oauth2/userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> getUserInfo();
}
