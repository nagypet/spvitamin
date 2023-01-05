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
import {HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError} from 'rxjs/operators';
import {throwError} from 'rxjs';
import {ToastrService} from 'ngx-toastr';
import {AuthService} from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor
{

  constructor(
    private toastr: ToastrService,
    private globalService: AuthService
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler)
  {
    /*
    // Authorization header will be inserted in AdminService
    const token = this.authService.getToken();
    const authorizationHeader = 'Bearer ' + token;
    console.log('Authorization header: ' + authorizationHeader);

    const modifiedReq = req.clone({
      headers: req.headers.set('Authorization', authorizationHeader)
    });
    */

    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status != 200)
        {
          let message = "Error";
          if (err.error !== null)
          {
            message = err.error.message;
          }
          this.toastr.error(message, "HTTP Error: " + err.status, {
            timeOut: 3000,
            progressBar: true,
            progressAnimation: 'increasing'
          });
          console.log('Unathorized access');
        }
        return throwError(err);
      })
    );
  }

}
