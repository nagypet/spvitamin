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

package hu.perit.spvitamin.spring.security.oauth2.idp.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants
{
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String SCOPE = "scope";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String BEARER = "bearer";
    public static final String EXPIRES_IN = "expires_in";

    public static final String OFFLINE_ACCESS = "offline_access";

    public static final int OAUTH2_CONTROLLER_BASE = 1000;
    public static final int OAUTH2_CONTROLLER_TOKEN = OAUTH2_CONTROLLER_BASE + 1;
    public static final int OAUTH2_CONTROLLER_REFRESH = OAUTH2_CONTROLLER_BASE + 2;
    public static final int OAUTH2_CONTROLLER_OID_CONFIG = OAUTH2_CONTROLLER_BASE + 3;
    public static final int OAUTH2_CONTROLLER_JWKS = OAUTH2_CONTROLLER_BASE + 4;
    public static final int OAUTH2_CONTROLLER_USER_INFO = OAUTH2_CONTROLLER_BASE + 5;
}
