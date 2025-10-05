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

import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, firstValueFrom, mergeMap, Observable, of, Subject, throwError, timer} from 'rxjs';
import {catchError, finalize, first, map, switchMap, tap} from 'rxjs/operators';
import {OAuthTokenResponse, OAuthUserInfo, OpenIdConfigurationResponse, StoredToken} from './oauth-models';
import {ConfigurableService} from '../auth/configurable.service';
import {AbstractAuthService} from '../auth/abstract-auth.service';


export interface SimpleOAuthConfig
{
  baseUrl: string;
  clientId?: string;
  clientSecret?: string;
  scope?: string;
}


export interface OAuthConfig extends SimpleOAuthConfig
{
  tokenEndpoint: string;
  userInfoEndpoint: string;
}

export function configureOAuthService(oAuthService: OAuthService, config: SimpleOAuthConfig)
{
  return firstValueFrom(
    oAuthService.autoconfigure(config).pipe(
      switchMap(() =>
        oAuthService.refreshToken().pipe(
          catchError(() => of(void 0))
        )
      ),
      catchError((err) =>
      {
        console.error('OAuth init error', err);
        return of(void 0);
      })
    )
  );
}


@Injectable({providedIn: 'root'})
export class OAuthService extends ConfigurableService<OAuthConfig> implements AbstractAuthService
{
  private token$ = new BehaviorSubject<StoredToken | null>(null);
  private userInfo$ = new BehaviorSubject<OAuthUserInfo | null>(null);
  private refreshing = false;
  private refreshQueue$ = new Subject<void>();
  private refreshTimerSub: any;

  private _displayName$ = this.userInfo$.pipe(map(u => u?.preferred_username ?? u?.name));
  public get displayName$(): Observable<string | undefined>
  {
    return this._displayName$;
  }


  constructor(private httpClient: HttpClient)
  {
    super();
  }


  autoconfigure(cfg: SimpleOAuthConfig): Observable<void>
  {
    // getting configuration using the .well-known endpoint
    return this.httpClient.get<OpenIdConfigurationResponse>(`${cfg.baseUrl}/.well-known/openid-configuration`).pipe(
      tap(response =>
      {
        console.log(`.well-known/openid-configuration successful for ${response.issuer}`);
        this.configure(
          {
            baseUrl: cfg.baseUrl,
            clientId: cfg.clientId,
            clientSecret: cfg.clientSecret,
            scope: cfg.scope,
            tokenEndpoint: response.token_endpoint,
            userInfoEndpoint: response.userinfo_endpoint
          }
        );
      }),
      map(() => void 0),
      catchError(err =>
      {
        console.log('.well-known/openid-configuration failed', err);
        return of(void 0);
      })
    );
  }


  get accessToken(): string | null
  {
    const t = this.token$.value;
    return t && Date.now() < t.expiresAt ? t.accessToken : null;
  }


  get isLoggedIn(): boolean
  {
    return this.accessToken !== null;
  }


  login(username: string, password: string): Observable<void>
  {
    const body = this.buildForm({
      grant_type: 'password',
      username,
      password,
      client_id: this.config.clientId,
      client_secret: this.config.clientSecret,
      scope: this.config.scope,
    });

    return this.httpClient.post<OAuthTokenResponse>(`${this.config.baseUrl}${this.config.tokenEndpoint}`, body.toString(), {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    }).pipe(
      tap(response =>
      {
        this.onTokenResponse(response);
        this.getProfile().subscribe();
      }),
      map(() => void 0)
    );
  }


  refreshToken(): Observable<void>
  {
    console.log('refreshToken()');

    if (this.refreshing)
    {
      return this.refreshQueue$.pipe(first(), map(() => void 0));
    }

    this.refreshing = true;
    const body = this.buildForm({
      grant_type: 'refresh_token',
      client_id: this.config.clientId,
      client_secret: this.config.clientSecret
    });

    return this.httpClient.post<OAuthTokenResponse>(`${this.config.baseUrl}${this.config.tokenEndpoint}`, body.toString(), {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    }).pipe(
      tap(response =>
      {
        this.onTokenResponse(response);
        this.getProfile().subscribe();
      }),
      finalize(() =>
      {
        this.refreshing = false;
        this.refreshQueue$.next();
      }),
      map(() => void 0),
      catchError(err =>
        {
          return this.logout().pipe(
            // ha a logout is hibázik, azt lenyeljük
            catchError(() => of(void 0)),
            // majd az eredeti hibát továbbdobjuk a hívónak
            mergeMap(() => throwError(() => err))
          );
        }
      )
    );
  }


  /**
   * logout
   */
  logout(): Observable<void>
  {
    console.log('logout');

    return this.httpClient.post<void>(`${this.config.baseUrl}/api/spvitamin/logout`, {}).pipe(
      tap(() =>
      {
        console.log('logout successful');
      }),
      catchError(() => of(void 0)),
      finalize(() =>
      {
        this.cleanUpSessionStorage();
      })
    );
  }


  /**
   * This checks if the session is already authenticated
   */
  getProfile(): Observable<OAuthUserInfo>
  {
    console.log('getProfile()');
    return new Observable<OAuthUserInfo>(observer =>
    {
      this.httpClient.get<OAuthUserInfo>(`${this.config.baseUrl}/api/spvitamin/oauth2/userinfo`).subscribe({
        next: userInfo =>
        {
          // OK
          console.log(`getProfile successful for ${userInfo.sub}`);
          this.userInfo$.next(userInfo);
          observer.next(userInfo);
        }, error: err =>
        {
          // error
          console.log('getProfile failed', err);
          observer.error(err);
        }
      });
    });
  }


  private cleanUpSessionStorage(): void
  {
    console.log('cleanUpSessionStorage()');
    this.clearRefreshTimer();
    this.token$.next(null);
    this.userInfo$.next(null);
  }


  private onTokenResponse(response: OAuthTokenResponse): void
  {
    console.log(`token expires in: ${response.expires_in} seconds`);
    const expiresAt = Date.now() + response.expires_in * 1000;
    const stored: StoredToken = {
      accessToken: response.access_token,
      expiresAt
    };
    this.token$.next(stored);
    this.scheduleRefresh(stored);
  }


  private scheduleRefresh(tokens: StoredToken): void
  {
    this.clearRefreshTimer();
    const delayMs = Math.max(0, tokens.expiresAt - Date.now() - 30000); // 30 mp ráhagyás
    this.refreshTimerSub = timer(delayMs).pipe(
      switchMap(() => this.refreshToken())
    ).subscribe({
      error: () => this.logout()
    });
  }


  private clearRefreshTimer(): void
  {
    if (this.refreshTimerSub)
    {
      this.refreshTimerSub.unsubscribe?.();
      this.refreshTimerSub = null;
    }
  }


  private buildForm(params: Record<string, string | undefined>): HttpParams
  {
    let form = new HttpParams();
    Object.entries(params).forEach(([k, v]) =>
    {
      if (v !== undefined)
      {
        form = form.set(k, v);
      }
    });
    return form;
  }


  ignoreInTokenInterceptor(url: string): boolean
  {
    return this.isConfigured ? url.includes(this.config.tokenEndpoint) : false;
  }
}
