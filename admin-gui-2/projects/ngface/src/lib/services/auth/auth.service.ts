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

import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, firstValueFrom, mergeMap, Observable, of, Subject, throwError, timer} from 'rxjs';
import {catchError, finalize, first, map, switchMap, tap} from 'rxjs/operators';
import {ConfigurableService} from './configurable.service';
import {SpvitaminSecurity} from './spvitamin-security-models';
import {AbstractAuthService} from './abstract-auth.service';

export interface AuthConfig
{
  baseUrl: string;
}


export function configureAuthService(authService: AuthService, config: AuthConfig)
{
  authService.configure(config);
  return firstValueFrom(
    authService.getProfile().pipe(catchError(() => of(null)))
  );
}


@Injectable({
  providedIn: 'root'
})
export class AuthService extends ConfigurableService<AuthConfig> implements AbstractAuthService
{
  private token$ = new BehaviorSubject<SpvitaminSecurity.AuthorizationToken | null>(null);
  private refreshing = false;
  private refreshQueue$ = new Subject<void>();
  private refreshTimerSub: any;

  private _displayName$ = this.token$.pipe(map(t => t?.preferred_username ?? t?.sub ?? 'Anonymous'));
  public get displayName$(): Observable<string | undefined>
  {
    return this._displayName$;
  }


  get accessToken(): string | null
  {
    const t = this.token$.value;
    return t && Date.now() < new Date(t.exp).getTime() ? t.jwt : null;
  }


  get isLoggedIn(): boolean
  {
    return this.accessToken !== null;
  }


  constructor(private httpClient: HttpClient)
  {
    super();
  }


  /**
   * This checks if the session is already authenticated
   */
  getProfile(): Observable<SpvitaminSecurity.AuthorizationToken>
  {
    console.log('getProfile()');
    return new Observable<SpvitaminSecurity.AuthorizationToken>(observer =>
    {
      this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(`${this.config.baseUrl}/api/spvitamin/authenticate`).subscribe({
        next: token =>
        {
          // OK
          console.log(`getProfile successful for ${token.sub}`);
          this.loginSuccess(token);
          observer.next(token);
        }, error: err =>
        {
          // error
          console.log('getProfile failed', err);
          this.token$.next(null);
          observer.error(err);
        }
      });
    });
  }


  /**
   * Calling auth with basic header to retrieve token
   * @param username
   * @param password
   * @param withInfo
   */
  login(username: string, password: string): Observable<void>
  {
    let headers = new HttpHeaders().append(
      'Authorization',
      'Basic ' + btoa(unescape(encodeURIComponent(`${username}:${password}`)))
    );

    return this.logout().pipe(
      switchMap(() => this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(
        `${this.config.baseUrl}/api/spvitamin/authenticate`,
        {
          headers,
          withCredentials: true
        })),
      tap(token => this.loginSuccess(token)),
      map(() => void 0),
      catchError(error =>
      {
        // ha a login hibás, attól még a logout már megtörtént
        return throwError(() => error);
      }),
    );
  }


  /**
   * logout
   */
  logout(withWarning?: boolean): Observable<void>
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


  private cleanUpSessionStorage(): void
  {
    console.log('cleanUpSessionStorage()');
    this.clearRefreshTimer();
    this.token$.next(null);
  }


  /**
   * Handle authorization errors from error interceptor
   * @param error
   */
  handleAuthError(error: HttpErrorResponse): void
  {
    this.logout().subscribe();
  }


  refreshToken(): Observable<void>
  {
    const token = this.token$.value;
    if (!token)
    {
      return of(void 0) as Observable<void>;
    }

    if (this.refreshing)
    {
      return this.refreshQueue$.pipe(first(), map(() => void 0));
    }

    const headers = new HttpHeaders().append('Authorization', 'Bearer ' + token.jwt);
    return this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(
      `${this.config.baseUrl}/api/spvitamin/authenticate`,
      {headers}
    ).pipe(
      tap(response =>
      {
        this.loginSuccess(response);
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


  private loginSuccess(token: SpvitaminSecurity.AuthorizationToken): void
  {
    console.log(`loginSuccess for ${token.sub}`);

    if (token)
    {
      const tokenValidSeconds = this.getTokenValidSeconds(token);
      console.log(`token expires in: ${tokenValidSeconds} seconds`);
    }
    this.token$.next(token);
    this.scheduleRefresh(token);
  }


  public getTokenValidSeconds(token: SpvitaminSecurity.AuthorizationToken): number
  {
    if (!token)
    {
      return 0;
    }

    return Math.round((new Date(token.exp).getTime() - new Date().getTime()) / 1000);
  }


  private scheduleRefresh(token: SpvitaminSecurity.AuthorizationToken): void
  {
    this.clearRefreshTimer();
    var validSeconds = this.getTokenValidSeconds(token);
    const delayMs = Math.max(0, validSeconds * 1000 - 30000); // 30 mp ráhagyás
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


  ignoreInTokenInterceptor(url: string): boolean
  {
    return url.includes('authenticate');
  }
}
