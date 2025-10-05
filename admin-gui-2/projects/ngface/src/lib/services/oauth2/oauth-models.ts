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

export interface OAuthTokenResponse
{
  access_token: string;
  expires_in: number;
  scope?: string;
}

export interface StoredToken
{
  accessToken: string;
  expiresAt: number;      // epoch millis
}

export interface OAuthUserInfo
{
  sub: string;
  name: string;
  preferred_username: string;
}


/**
 * {
 *     "jwks_uri": "https://localhost:8410/.well-known/jwks.json",
 *     "grant_types_supported": [
 *         "refresh_token",
 *         "password"
 *     ],
 *     "userinfo_endpoint": "https://localhost:8410/api/spvitamin/oauth2/userinfo",
 *     "token_endpoint": "https://localhost:8410/api/spvitamin/oauth2/token",
 *     "token_endpoint_auth_methods_supported": [
 *         "client_secret_basic",
 *         "client_secret_post"
 *     ],
 *     "issuer": "https://localhost:8410"
 * }
 */
export interface OpenIdConfigurationResponse
{
  issuer: string;
  token_endpoint: string;
  userinfo_endpoint: string;
  jwks_uri: string;
}
