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
import {Component, OnInit} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {AuthService} from '../../services/auth/auth.service';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {NgIf} from '@angular/common';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatBadge} from '@angular/material/badge';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  imports: [
    NgIf,
    RouterLink,
    MatToolbar,
    RouterLinkActive,
    MatIcon,
    MatIconButton,
    MatButton,
    MatBadge
  ],
  standalone: true
})
export class HeaderComponent implements OnInit
{
  public title = '';
  public version = '';

  constructor
  (
    private adminService: AdminService,
    public authService: AuthService,
    private router: Router,
  )
  {

  }

  ngOnInit()
  {
    this.adminService.getVersionInfo().subscribe(data =>
    {
      console.log(data);
      this.title = data.Title;
      this.version = data.Version;
    });

    this.authService.getProfile().subscribe();
  }

  onLogout()
  {
    this.authService.logout();
    this.router.navigateByUrl('/');
  }
}
