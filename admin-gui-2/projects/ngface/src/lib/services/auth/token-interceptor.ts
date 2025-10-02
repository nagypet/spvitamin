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

/* tslint:disable:one-line */
import {Injectable, Injector} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, switchMap, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthenticationRepositoryService} from './authentication-repository.service';

@Injectable()
export class TokenInterceptor implements HttpInterceptor
{
  constructor(private injector: Injector)
  {
  }


  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
  {
    const repositoryService = this.injector.get(AuthenticationRepositoryService);
    const authService = repositoryService.authService;
    if (!authService || !authService.isConfigured || request.headers.has('Authorization') || authService.ignoreInTokenInterceptor(request.url))
    {
      return next.handle(request);
    }

    const token = authService.accessToken;
    const authReq = token
      ? request.clone({setHeaders: {Authorization: `Bearer ${token}`}})
      : request;

    return next.handle(authReq).pipe(
      catchError((err: HttpErrorResponse) =>
      {
        if (err.status === 401 && authService.accessToken)
        {
          return authService.refreshToken().pipe(
            switchMap(() =>
            {
              const newToken = authService.accessToken;
              const retried = newToken
                ? request.clone({setHeaders: {Authorization: `Bearer ${newToken}`}})
                : request;
              return next.handle(retried);
            }),
            catchError(refreshErr => throwError(() => refreshErr))
          );
        }
        return throwError(() => err);
      })
    );
  }
}
