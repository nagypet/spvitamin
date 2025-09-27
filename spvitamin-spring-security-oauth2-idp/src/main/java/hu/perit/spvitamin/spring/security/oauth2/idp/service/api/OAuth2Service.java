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

package hu.perit.spvitamin.spring.security.oauth2.idp.service.api;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface OAuth2Service
{
    ResponseEntity<Map<String, Object>> token(MultiValueMap<String, String> form);

    ResponseEntity<Map<String, Object>> refresh(MultiValueMap<String, String> form);

    ResponseEntity<Map<String, Object>> openidConfiguration();

    ResponseEntity<Map<String, Object>> getUserInfo();
}
