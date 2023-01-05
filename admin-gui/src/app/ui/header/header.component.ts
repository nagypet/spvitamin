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

import {Component, OnInit} from '@angular/core';
import {AdminService} from '../admin.service';
import {AuthService} from '../auth/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  public title = '';
  public version = '';

  constructor
  (
    private adminService: AdminService,
    public authService: AuthService,
    private router: Router,
  ) {

  }

  ngOnInit() {
    this.adminService.getVersionInfo().subscribe(data => {
      console.log(data);
      this.title = data.Title;
      this.version = data.Version;
    });
  }

  onLogout() {
    this.authService.logout().subscribe().add(() => {
      this.authService.tryGetSettings().subscribe().add(() => {
        this.router.navigateByUrl('/');
      });
    });
  }
}
