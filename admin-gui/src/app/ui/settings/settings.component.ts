/* tslint:disable:one-line */
/*
 * Copyright 2020-2023 the original author or authors.
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
import {AdminService} from '../../services/admin.service';
import {AuthService} from '../../services/auth/auth.service';

export class ServerParameter
{
  name: string;
  value: string;
  link: boolean;

  constructor(name, value, link)
  {
    this.name = name;
    this.value = value;
    this.link = link;
  }
}

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit
{
  public settings: Array<ServerParameter> | null = new Array<ServerParameter>();
  public shutdownIsInProgress = false;

  constructor(
    public adminService: AdminService,
    public authService: AuthService
  )
  {

  }

  ngOnInit()
  {
    this.adminService.getSettings().subscribe(data =>
    {
      this.settings = data;
    }, error =>
    {
      this.settings = null;
    });

    this.authService.isLoggedIn.subscribe(value =>
    {
      if (!value)
      {
        this.settings = null;
      }
    });
  }


  onShutdown()
  {
    this.adminService.postShutdown().subscribe(data =>
    {
      this.shutdownIsInProgress = true;
    });
  }
}
