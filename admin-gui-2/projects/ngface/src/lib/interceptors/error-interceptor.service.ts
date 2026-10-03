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
import {Injectable, Optional} from '@angular/core';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {ErrorService} from '../services/error.service';
import {OAuthService} from '../services/oauth2/oauth.service';
import {ErrorInterceptorCustomizer} from './error-interceptor-customizer';
import {DefaultErrorInterceptorCustomizer} from './default-error-interceptor-customizer';


@Injectable()
export class ErrorInterceptor implements HttpInterceptor
{

  constructor(
    private errorService: ErrorService,
    private oAuthService: OAuthService,
    private defaultCustomizer: DefaultErrorInterceptorCustomizer,
    @Optional() private customizer: ErrorInterceptorCustomizer
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

          const tokenEndpoint: boolean = this.oAuthService.isConfigured ? request.url.includes(this.oAuthService.config.tokenEndpoint) : false;

          if (request.url.includes('/logout') || tokenEndpoint)
          {
            console.error('error', error);
          }
          else
          {
            const activeCustomizer = this.customizer ?? this.defaultCustomizer;
            if (activeCustomizer.shouldHandleError(request, error))
            {
              this.errorService.handleError(error);
            }
          }
          return throwError(() => error);
        })
      );
  }
}
