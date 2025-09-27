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
