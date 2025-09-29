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
