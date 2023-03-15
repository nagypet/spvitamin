/* tslint:disable:one-line */
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthService} from '../services/auth/auth.service';
import {ErrorService} from '../services/error.service';


@Injectable()
export class ErrorInterceptor implements HttpInterceptor
{

  constructor(private authService: AuthService, private errorService: ErrorService)
  {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
  {
    return next.handle(request)
      .pipe(
        catchError((error: HttpErrorResponse) =>
        {
          console.error(error);

          if (error.status === 401)
          {
            this.authService.handleAuthError(error);
          }

          if (error.status !== 401)
          {
            this.errorService.handleError(error);
          }
          return throwError(error);
        })
      );
  }
}
