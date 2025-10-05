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

import {firstValueFrom, from, of} from 'rxjs';
import {catchError, switchMap, tap} from 'rxjs/operators';
import {configureOAuthService, OAuthService, SimpleOAuthConfig} from '../oauth2/oauth.service';
import {AuthenticationRepositoryService} from './authentication-repository.service';
import {AuthConfig, AuthService, configureAuthService} from './auth.service';
import {SpvitaminSecurity} from './spvitamin-security-models';


export function configureSecurity(
  repositoryService: AuthenticationRepositoryService,
  authService: AuthService, authConfig: AuthConfig,
  oAuthService: OAuthService, oAuthConfig: SimpleOAuthConfig
)
{
  return firstValueFrom(
    repositoryService.getAuthenticationRepository().pipe(
      switchMap(authRepo =>
      {
        if (isSpvitaminOAuthSupported(authRepo))
        {
          repositoryService.authService = oAuthService;
          // Promise -> Observable
          return from(configureOAuthService(oAuthService, oAuthConfig));
        }
        else
        {
          repositoryService.authService = authService;
          // Promise -> Observable
          return from(configureAuthService(authService, authConfig));
        }
      }),
      catchError(err =>
      {
        console.error('Security lib init error', err);
        return of(void 0);
      })
    )
  );
}


function isSpvitaminOAuthSupported(authRepo: SpvitaminSecurity.AuthenticationRepository)
{
  return authRepo.authenticationTypes.filter(authType => authType.type === 'oauth2' && authType.provider === 'spvitamin').length > 0;
}
