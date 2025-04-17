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
import {BehaviorSubject, mapTo, Observable, of, switchMap, throwError} from 'rxjs';
import {CookieService} from 'ngx-cookie-service';
import {ToastrService} from 'ngx-toastr';
import {environment} from '../../../../environments/environment';
import {catchError, finalize, map, tap} from 'rxjs/operators';
import {SpvitaminSecurity} from '../../../model/spvitamin-security-models';

@Injectable({
  providedIn: 'root'
})
export class AuthService
{
  private loginSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  public loggedIn$ = this.loginSubject$.asObservable();

  private loginPageVisibleSubject$ = new BehaviorSubject<boolean>(false);
  public loginPageVisible$ = this.loginPageVisibleSubject$.asObservable();

  public isRefreshing = false;

  setLoginPageVisible(value: boolean)
  {
    this.loginPageVisibleSubject$.next(value);
  }

  get isLoginPageVisible(): boolean
  {
    return this.loginPageVisibleSubject$.value;
  }


  get isLoggedIn(): boolean
  {
    return this.loginSubject$.value;
  }


  constructor(private httpClient: HttpClient,
              private cookieService: CookieService,
              private toastrService: ToastrService
  )
  {
    this.checkToken().subscribe();
  }


  /**
   * This checks if the session is already authenticated
   */
  getProfile(): Observable<SpvitaminSecurity.AuthorizationToken>
  {
    console.log('getProfile()');
    return new Observable<SpvitaminSecurity.AuthorizationToken>(observer =>
    {
      this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(`${environment.baseURL}/api/spvitamin/authenticate`).subscribe({
        next: token =>
        {
          // OK
          console.log(`getProfile successful for ${token.sub}`);
          this.loginSuccess(token, false);
          observer.next(token);
        }, error: err =>
        {
          // error
          console.log('getProfile failed', err);
          this.loginSubject$.next(false);
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
  login(username: string, password: string, withInfo: boolean = true): Observable<SpvitaminSecurity.AuthorizationToken>
  {
    this.isRefreshing = true;

    let headers = new HttpHeaders().append(
      'Authorization',
      'Basic ' + btoa(unescape(encodeURIComponent(`${username}:${password}`)))
    );

    return this.logout().pipe(
      switchMap(() => this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(
        `${environment.baseURL}/api/spvitamin/authenticate`,
        {
          headers,
          withCredentials: true
        })),
      tap(token => this.loginSuccess(token, withInfo)),
      catchError(error =>
      {
        // ha a login hibás, attól még a logout már megtörtént
        return throwError(() => error);
      }),
      finalize(() => this.isRefreshing = false)
    );
  }


  loginFrontendAnonym(): Observable<void>
  {
    console.log('loginFrontendAnonym()');
    return this.login('frontend-anonym', 'an0n1m', false).pipe(
      catchError(err =>
      {
        console.error('loginFrontendAnonym() failed', err);
        return throwError(() => err);
      }),
      mapTo(void 0)
    );
  }


  /**
   * logout
   */
  logout(withWarning?: boolean): Observable<void>
  {
    console.log('logout');

    const hasToken = !!this.getToken();

    if (!hasToken)
    {
      this.cleanUpSessionStorage();
      return of(void 0); // nincs token, azonnal visszatérünk
    }

    return this.httpClient.post(`${environment.baseURL}/api/spvitamin/logout`, {}).pipe(
      tap(() =>
      {
        if (withWarning)
        {
          this.toastrService.warning('Please login again!', 'Session expired!');
        }

        this.loginSubject$.next(false);
      }),
      mapTo(void 0), // típus: Observable<void>
      finalize(() => this.cleanUpSessionStorage())
    );
  }


  private cleanUpSessionStorage(): void
  {
    sessionStorage.removeItem('token');
    this.cookieService.deleteAll();
  }

  /**
   * Handle authorization errors from error interceptor
   * @param error
   */
  handleAuthError(error: HttpErrorResponse): void
  {
    this.logout().subscribe();
  }


  private renewToken$(token: SpvitaminSecurity.AuthorizationToken): Observable<SpvitaminSecurity.AuthorizationToken>
  {
    const headers = new HttpHeaders().append('Authorization', 'Bearer ' + token.jwt);
    return this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(
      `${environment.baseURL}/api/spvitamin/authenticate`,
      {headers}
    );
  }


  renewToken(): void
  {
    console.log('renewToken()');

    const token = this.getToken();
    if (!token)
    {
      return;
    }

    this.renewToken$(token).subscribe({
      next: (token) => this.loginSuccess(token, false),
      error: () => this.logout().subscribe()
    });
  }


  private loginSuccess(token: SpvitaminSecurity.AuthorizationToken, withInfo: boolean = true): void
  {
    console.log(`loginSuccess for ${token.sub}`);

    sessionStorage.setItem('token', JSON.stringify(token));
    if (token)
    {
      const tokenValidSeconds = this.getTokenValidSeconds(token);
      const tokenValidMinutes = Math.round(tokenValidSeconds / 60);
      console.log('time until expire', tokenValidSeconds);
      if (withInfo)
      {
        this.toastrService.success(`Session validity: ${tokenValidMinutes} minutes`, `Welcome ${this.getDisplayName()}`);
      }
    }
    this.loginSubject$.next(true);
  }


  public checkToken(t?: SpvitaminSecurity.AuthorizationToken): Observable<SpvitaminSecurity.AuthorizationToken | undefined>
  {
    const token = t ?? this.getToken();

    if (!token)
    {
      return this.logout(true).pipe(map(() => undefined));
    }

    const tokenValidSeconds = this.getTokenValidSeconds(token);
    console.log('checkToken() sub: \'' + token.sub + '\' valid: ' + tokenValidSeconds + ' seconds');

    if (tokenValidSeconds > 0)
    {
      this.loginSubject$.next(true);
      return of(token);
    }
    else
    {
      return this.logout(true).pipe(map(() => undefined));
    }
  }


  public getTokenValidSeconds(token?: SpvitaminSecurity.AuthorizationToken): number
  {
    if (!token)
    {
      return 0;
    }
    return Math.round((new Date(token.exp).getTime() - new Date().getTime()) / 1000);
  }


  public getToken(): SpvitaminSecurity.AuthorizationToken | undefined
  {
    const tokenString = sessionStorage.getItem('token');
    if (tokenString === null)
    {
      return undefined;
    }
    return JSON.parse(tokenString);
  }


  getUserName(): string
  {
    return this.getToken()?.sub ?? 'Anonymous';
  }


  getDisplayName(): string
  {
    return this.getToken()?.preferred_username ?? this.getUserName();
  }
}
