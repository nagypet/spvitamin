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
import {Component, OnInit} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {AuthService} from '../../services/auth/auth.service';
import {NgForOf, NgIf} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {MatCardModule} from '@angular/material/card';
import {Spvitamin} from '../../spvitamin-admin-models';
import ServerParameter = Spvitamin.ServerParameter;
import ServerSettingsResponse = Spvitamin.ServerSettingsResponse;


@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
  imports: [
    NgForOf,
    NgIf,
    MatButtonModule,
    MatTooltip,
    MatCardModule
  ],
  standalone: true
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
    this.adminService.getSettings().subscribe((data: ServerSettingsResponse) =>
    {
      this.settings = data.serverParameters;
    }, error =>
    {
      this.settings = null;
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
