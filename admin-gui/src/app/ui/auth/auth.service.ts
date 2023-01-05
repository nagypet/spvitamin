/*
 * Copyright 2020-2022 the original author or authors.
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

import {Injectable} from '@angular/core';
import {HttpBackend, HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {AuthToken} from '../../modell/auth-model';
import {environment} from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AuthService
{

  private httpSilent: HttpClient;

  private authenticated = false;
  private userName = '';
  private token = '';
  private settingsAreAvailable = false;

  constructor(
    private http: HttpClient,
    private handler: HttpBackend
  )
  {
    this.httpSilent = new HttpClient(handler);
  }


  public isAuthenticated(): boolean
  {
    return this.authenticated;
  }


  public getUserName(): string
  {
    return this.userName;
  }


  public getToken(): string
  {
    return this.token;
  }


  public areSettingsAvailable(): boolean
  {
    return this.settingsAreAvailable;
  }


  public authenticateWithUserNamePassword(userName: string, password: string): Observable<any>
  {
    return this.http.get(`${environment.baseURL}/api/spvitamin/authenticate`, this.getAuthHeaderForLegacyAuthentication(userName, password)).pipe(
      tap(response =>
      {
        console.log('authenticateWithUserNamePassword()');
        console.log(response);
        this.authenticated = true;
        this.userName = response.sub;
        this.token = response.jwt;
      }, err =>
      {
        console.log(err);
        this.authenticated = false;
        this.userName = '';
        this.token = '';
      })
    );
  }


  public getAuthentication(): Observable<any>
  {
    return this.httpSilent.get<AuthToken>(`${environment.baseURL}/api/spvitamin/authenticate`).pipe(
      tap(response =>
      {
        console.log('getAuthentication(): Session already authenticated!');
        this.authenticated = true;
        this.userName = response.sub;
        this.token = response.jwt;
      }, err =>
      {
        console.log('getAuthentication() error: ' + err.status + ' Session is not authenticated!');
        console.log(err);
        this.authenticated = false;
        this.userName = '';
        this.token = '';
      })
    );
  }


  public tryGetSettings(): Observable<any>
  {
    return this.httpSilent.get(`${environment.baseURL}/api/spvitamin/admin/settings`).pipe(
      tap(response =>
      {
        console.log('tryGetSettings(): Authentication is not required, settings can be displayed!');
        this.settingsAreAvailable = true;
      }, err =>
      {
        console.log('tryGetSettings() error: ' + err.status + ' authentication is required, no settings can be displayed!');
        this.settingsAreAvailable = false;
      })
    );
  }


  public logout(): Observable<any>
  {
    return this.http.get(`${environment.baseURL}/logout`).pipe(
      tap(response =>
      {
        this.authenticated = false;
        this.userName = '';
        this.token = '';
      }, err =>
      {
        console.log(err);
        this.authenticated = false;
        this.userName = '';
        this.token = '';
      })
    );
  }


  private getAuthHeaderForLegacyAuthentication(userName?: String, password?: String): { headers: HttpHeaders }
  {
    let authorizationHeader = '';
    if (userName !== undefined && password !== undefined)
    {
      const credentials = userName + ':' + password;
      authorizationHeader = 'Basic ' + btoa(credentials);
    } else
    {
      const credentials = this.getToken();
      authorizationHeader = 'Bearer ' + credentials;
    }

    // console.log(credentials);

    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': authorizationHeader
      })
    };
  }


  private getAuthHeaderForToken(token: String): { headers: HttpHeaders }
  {
    const authorizationHeader = 'Bearer ' + token;

    return {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': authorizationHeader
      })
    };
  }
}
