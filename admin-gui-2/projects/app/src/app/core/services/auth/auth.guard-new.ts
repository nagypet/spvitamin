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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthService} from './auth.service';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';


@Injectable()
export class AuthGuard implements CanActivate
{
  constructor(public authService: AuthService, public router: Router)
  {
  }


  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean>
  {
    return this.authService.checkToken().pipe(
      map(token =>
      {
        if (!token)
        {
          this.router.navigate(['public']);
          return false;
        }

        const allowedForAnonym = ['/public', '/login'];

        if (token.sub === 'frontend-anonym')
        {
          const isAllowed = allowedForAnonym.some(path => state.url.startsWith(path));
          if (!isAllowed)
          {
            this.router.navigate(['public']);
          }
          return isAllowed;
        }

        return true;
      }),
      catchError(() =>
      {
        this.router.navigate(['public']);
        return of(false);
      })
    );
  }
}
