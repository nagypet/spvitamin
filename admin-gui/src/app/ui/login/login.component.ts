/*
 * Copyright 2020-2024 the original author or authors.
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
import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AuthService} from '../../services/auth/auth.service';
import {Location} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {ErrorService} from '../../services/error.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: true
})
export class LoginComponent implements OnInit
{
  @ViewChild('usernameInput', {static: true}) usernameInput: ElementRef;
  @ViewChild('passwordInput', {static: true}) passwordInput: ElementRef;

  returnUrl: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private errorService: ErrorService
  )
  {
  }

  ngOnInit()
  {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  onLogin()
  {
    const userName = this.usernameInput.nativeElement.value;
    const password = this.passwordInput.nativeElement.value;

    this.authService.login(userName, password).subscribe(data =>
    {
      this.router.navigateByUrl(this.returnUrl);
    }, error =>
    {
      this.errorService.errorToast('', 'Invalid username or password!');
    });

  }

  onCancel()
  {
    this.authService.logout();
    this.router.navigateByUrl('/');
  }


  onKeyPress(event: KeyboardEvent)
  {
    //console.log("onKeyPress " + event.key);
    if (event.key === 'Enter')
    {
      this.onLogin();
    } else if (event.key === 'Escape')
    {
      this.onCancel();
    }
  }
}
