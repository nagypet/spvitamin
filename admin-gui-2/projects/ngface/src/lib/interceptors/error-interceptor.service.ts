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

import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {ErrorService} from '../services/error.service';
import {OAuthService} from '../services/oauth2/oauth.service';


@Injectable()
export class ErrorInterceptor implements HttpInterceptor
{

  constructor(
    private errorService: ErrorService,
    private oAuthService: OAuthService
  )
  {
  }


  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
  {
    return next.handle(request)
      .pipe(
        catchError((error: HttpErrorResponse) =>
        {
          console.error(error);

          if (error.status !== 401 && error.status !== 404)
          {
            if (!request.url.includes('/logout') && this.oAuthService.isConfigured
              && !request.url.includes(this.oAuthService.config.tokenEndpoint)
            )
            {
              this.errorService.handleError(error);
            }
          }
          return throwError(() => error);
        })
      );
  }
}
