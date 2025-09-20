// TypeScript
import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {SpvitaminSecurity} from '../../model/spvitamin-security-models';

@Injectable({providedIn: 'root'})
export class TokenStoreService
{
  private tokenSubject = new BehaviorSubject<SpvitaminSecurity.AuthorizationToken | null>(null);

  // Feliratkozáshoz (ha kell)
  token$: Observable<SpvitaminSecurity.AuthorizationToken | null> = this.tokenSubject.asObservable();


  // Szinchr. lekéréshez
  getToken(): SpvitaminSecurity.AuthorizationToken | null
  {
    return this.tokenSubject.value;
  }


  setToken(token: SpvitaminSecurity.AuthorizationToken | null): void
  {
    this.tokenSubject.next(token);
  }


  clear(): void
  {
    this.tokenSubject.next(null);
  }
}
