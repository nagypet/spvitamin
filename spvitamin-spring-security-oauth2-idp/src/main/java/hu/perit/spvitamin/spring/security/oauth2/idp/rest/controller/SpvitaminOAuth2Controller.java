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

package hu.perit.spvitamin.spring.security.oauth2.idp.rest.controller;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import hu.perit.spvitamin.spring.security.oauth2.idp.config.Constants;
import hu.perit.spvitamin.spring.security.oauth2.idp.rest.api.SpvitaminOAuth2Api;
import hu.perit.spvitamin.spring.security.oauth2.idp.service.api.JwkService;
import hu.perit.spvitamin.spring.security.oauth2.idp.service.api.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpvitaminOAuth2Controller implements SpvitaminOAuth2Api
{
    private final OAuth2Service oAuth2Service;
    private final JwkService jwkService;


    @Override
    @LoggedRestMethod(eventId = Constants.OAUTH2_CONTROLLER_TOKEN)
    public ResponseEntity<Map<String, Object>> token(MultiValueMap<String, String> form)
    {
        try
        {
            return this.oAuth2Service.token(form);
        }
        catch (Exception e)
        {
            return error(e);
        }
    }


    @Override
    @LoggedRestMethod(eventId = Constants.OAUTH2_CONTROLLER_REFRESH)
    public ResponseEntity<Map<String, Object>> refresh(MultiValueMap<String, String> form)
    {
        try
        {
            return this.oAuth2Service.refresh(form);
        }
        catch (Exception e)
        {
            return error(e);
        }
    }


    @Override
    @LoggedRestMethod(eventId = Constants.OAUTH2_CONTROLLER_OID_CONFIG)
    public ResponseEntity<Map<String, Object>> openidConfiguration()
    {
        try
        {
            return this.oAuth2Service.openidConfiguration();
        }
        catch (Exception e)
        {
            return error(e);
        }
    }


    @Override
    @LoggedRestMethod(eventId = Constants.OAUTH2_CONTROLLER_JWKS)
    public ResponseEntity<Map<String, Object>> jwks() throws Exception
    {
        try
        {
            return ResponseEntity.ok(jwkService.getJwks());
        }
        catch (Exception e)
        {
            return error(e);
        }
    }


    @Override
    @LoggedRestMethod(eventId = Constants.OAUTH2_CONTROLLER_USER_INFO)
    public ResponseEntity<Map<String, Object>> getUserInfo()
    {
        try
        {
            return this.oAuth2Service.getUserInfo();
        }
        catch (Exception e)
        {
            return error(e);
        }
    }


    private static ResponseEntity<Map<String, Object>> error(Exception e)
    {
        log.error(StackTracer.toString(e));

        Map<String, Object> m = new HashMap<>();
        m.put("error", "server_error");
        m.put("error_description", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName() + " occurred.");
        return ResponseEntity.internalServerError().body(m);
    }
}
