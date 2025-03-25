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
import {BehaviorSubject, Observable} from 'rxjs';
import {CookieService} from 'ngx-cookie-service';
import {ToastrService} from 'ngx-toastr';
import {environment} from '../../../../environments/environment';
import {SpvitaminSecurity} from '../../../model/spvitamin-security-models';

@Injectable({
  providedIn: 'root'
})
export class AuthService
{
  private loginSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  get isLoggedIn(): boolean
  {
    return this.loginSubject$.value;
  }
  public loggedIn$ = this.loginSubject$.asObservable();

  constructor(private httpClient: HttpClient,
              private cookieService: CookieService,
              private toastrService: ToastrService
  )
  {
    this.checkToken();
  }


  /**
   * This checks if the session is already authenticated
   */
  getProfile(): Observable<SpvitaminSecurity.AuthorizationToken>
  {
    return new Observable<SpvitaminSecurity.AuthorizationToken>(observer =>
    {
      this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(`${environment.baseURL}/api/spvitamin/authenticate`).subscribe(token =>
      {
        // OK
        this.loginSuccess(token);
        observer.next(token);
      }, error =>
      {
        // error
        this.loginSubject$.next(false);
        observer.error(error);
      });
    });
  }


  /**
   * Calling auth with basic header to retrieve token
   * @param username
   * @param password
   */
  login(username: string, password: string): Observable<SpvitaminSecurity.AuthorizationToken>
  {
    return new Observable<SpvitaminSecurity.AuthorizationToken>(observer =>
    {
      let headers = new HttpHeaders();
      // Accended characters fix
      headers = headers.append('Authorization', 'Basic ' + btoa(unescape(encodeURIComponent(username + ':' + password))));
      this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(`${environment.baseURL}/api/spvitamin/authenticate`, {headers}).subscribe(token =>
      {
        // OK
        this.loginSuccess(token);
        observer.next(token);
      }, error =>
      {
        // error
        this.logout();
        observer.error(error);
      });
    });
  }

  /**
   * logout
   */
  logout(withWarning?: boolean): void
  {
    console.log('logout');

    if (this.getToken())
    {
      if (withWarning)
      {
        this.toastrService.warning('Please login again!', 'Session expired!');
      }
      this.httpClient.post(`${environment.baseURL}/api/spvitamin/logout`, {}).subscribe(res =>
      {
        console.log(res);
        this.loginSubject$.next(false);
      });
    }
    this.cleanUpSessionStorage();
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
    this.logout();
  }

  renewToken(): void
  {
    const token = this.getToken();
    if (!token)
    {
      return;
    }

    let headers = new HttpHeaders();
    headers = headers.append('Authorization', 'Bearer ' + token.jwt);
    this.httpClient.get<SpvitaminSecurity.AuthorizationToken>(`${environment.baseURL}/api/spvitamin/authenticate`, {headers}).subscribe(token =>
    {
      this.loginSuccess(token, false);
    }, error =>
    {
      this.logout();
    });
  }

  private loginSuccess(token: SpvitaminSecurity.AuthorizationToken, withInfo: boolean = true): void
  {
    console.log('loginSuccess');

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


  public checkToken(t?: SpvitaminSecurity.AuthorizationToken): SpvitaminSecurity.AuthorizationToken | undefined
  {
    const token = t ?? this.getToken();
    if (!token)
    {
      this.logout(true);
      return undefined;
    }

    const tokenValidSeconds = this.getTokenValidSeconds(token);
    console.log('checkToken() sub: \'' + token.sub + '\' valid: ' + tokenValidSeconds + ' seconds');

    if (tokenValidSeconds > 0)
    {
      this.loginSubject$.next(true);
      return token;
    }
    else
    {
      this.logout(true);
      return undefined;
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
