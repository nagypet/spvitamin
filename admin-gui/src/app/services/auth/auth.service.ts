/* tslint:disable:one-line */
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {AuthToken} from './token-model';
import {ErrorService} from '../error.service';
import {CookieService} from 'ngx-cookie-service';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService
{

  public isLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private httpClient: HttpClient,
              private cookieService: CookieService,
              private errorService: ErrorService
  )
  {
  }


  getProfile(): Observable<AuthToken>
  {
    return new Observable<AuthToken>(observer =>
    {
      this.httpClient.get<AuthToken>(`${environment.baseURL}/api/spvitamin/authenticate`).subscribe(token =>
      {
        // OK
        this.loginSuccess(token);
        observer.next(token);
      }, error =>
      {
        // error
        observer.error(error);
      });
    });
  }

  /**
   * Calling auth with basic header to retreive token
   * @param username
   * @param password
   */
  login(username: string, password: string): Observable<AuthToken>
  {
    return new Observable<AuthToken>(observer =>
    {
      let headers = new HttpHeaders();
      // Accended characters fix
      headers = headers.append('Authorization', 'Basic ' + btoa(unescape(encodeURIComponent(username + ':' + password))));
      this.httpClient.get<AuthToken>(`${environment.baseURL}/api/spvitamin/authenticate`, {headers}).subscribe(token =>
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
        this.errorService.warningToast('Munkamenete lejárt!', 'Kérjük jelentkezzen be újra!');
      }
      // TODO implement logout endpoint
      // this.httpClient.post(`${environment.baseURL}/logout`, {}).subscribe(res => console.log(res));
      this.cleanUpSessionStorage();
      this.isLoggedIn.next(false);
    }
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
    this.httpClient.get<AuthToken>(`${environment.baseURL}/api/spvitamin/authenticate`, {headers}).subscribe(token =>
    {
      this.loginSuccess(token, false);
    }, error =>
    {
      this.logout();
    });
  }

  private loginSuccess(token: AuthToken, withInfo: boolean = true): void
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
        this.errorService.successToast('Bejelentkezés sikeres!', `Munkamenet érvényesség: ${tokenValidMinutes} perc`);
      }
    }
    this.isLoggedIn.next(true);
  }


  public checkToken(): AuthToken | undefined
  {
    const token = this.getToken();
    if (!token)
    {
      this.logout(true);
      return undefined;
    }

    const tokenValidSeconds = this.getTokenValidSeconds(token);
    console.log('checkToken() sub: \'' + token.sub + '\' valid: ' + tokenValidSeconds + ' seconds');

    if (tokenValidSeconds > 0)
    {
      this.isLoggedIn.next(true);
      return token;
    } else
    {
      this.logout(true);
      return undefined;
    }
  }


  public getTokenValidSeconds(token?: AuthToken): number
  {
    if (!token)
    {
      return 0;
    }
    return Math.round((new Date(token.exp).getTime() - new Date().getTime()) / 1000);
  }


  public getToken(): AuthToken | undefined
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

  isAuthenticated(): boolean
  {
    return this.isLoggedIn.value;
  }
}
