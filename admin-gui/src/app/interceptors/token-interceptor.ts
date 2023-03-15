/* tslint:disable:one-line */
import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthService} from '../services/auth/auth.service';
import {Observable} from 'rxjs';

@Injectable()
export class TokenInterceptor implements HttpInterceptor
{

  constructor(public authService: AuthService)
  {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>>
  {
    // If authentication is occuring no need to inject token.
    if (request.url.includes('authenticate'))
    {
      return next.handle(request);
    }

    const token = this.authService.getToken();
    if (!token)
    {
      return next.handle(request);
    }

    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token.jwt}`
      }
    });
    return next.handle(request);
  }
}
