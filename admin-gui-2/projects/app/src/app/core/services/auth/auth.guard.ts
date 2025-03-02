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
import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {Observable, of} from 'rxjs';
import {AuthService} from './auth.service';
import {AdminService} from '../admin.service';
import {catchError, map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate
{

  constructor(
    private authService: AuthService,
    private adminService: AdminService,
    private router: Router)
  {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    | boolean
    | UrlTree
    | Promise<boolean | UrlTree>
    | Observable<boolean | UrlTree>
  {
    return this.activateHandler(state);
  }

  activateHandler(state: RouterStateSnapshot):
    | boolean
    | UrlTree
    | Promise<boolean | UrlTree>
    | Observable<boolean | UrlTree>
  {

    if (this.authService.isLoggedIn)
    {
      return true;
    }

    return this.adminEndpointsAuthenticated().pipe(
      map((adminValid: boolean) =>
      {
        if (adminValid)
        {
          return true;
        } else
        {
          return this.router.createUrlTree(['/admin-gui/login'], {queryParams: {returnUrl: state.url}});
        }
      })
    );
  }


  private adminEndpointsAuthenticated(): Observable<boolean>
  {
    return this.adminService.getSettings().pipe(
      map(() =>
      {
        console.log('settings endpoint is accessable');
        // Ha a kérés sikeres, visszaadunk egy true értéket
        return true;
      }),
      catchError((error) =>
      {
        console.warn('settings endpoint is not accessable');
        // Ha a kérés 401-es hibát dob, akkor false-t adunk vissza
        if (error.status === 401)
        {
          return of(false);
        }
        return of(false);
      })
    );
  }
}
