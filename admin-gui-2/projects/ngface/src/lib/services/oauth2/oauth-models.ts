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
