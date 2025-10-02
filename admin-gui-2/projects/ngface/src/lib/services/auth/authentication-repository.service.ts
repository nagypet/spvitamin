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

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../../app/src/environments/environment';
import {SpvitaminSecurity} from './spvitamin-security-models';
import {AbstractAuthService} from './abstract-auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationRepositoryService
{
  private readonly serviceUrl = '/api/spvitamin/authentication-repository';
  private _authService?: AbstractAuthService;
  public set authService(authService: AbstractAuthService)
  {
    this._authService = authService;
  }
  public get authService(): AbstractAuthService | undefined
  {
    return this._authService;
  }

  constructor(private httpClient: HttpClient)
  {
  }


  public getAuthenticationRepository(): Observable<SpvitaminSecurity.AuthenticationRepository>
  {
    return this.httpClient.get(`${environment.baseURL}${this.serviceUrl}`) as Observable<SpvitaminSecurity.AuthenticationRepository>;
  }
}
