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
import {NgForOf, NgIf} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {MatCardModule} from '@angular/material/card';
import {AdminService} from '../../core/services/admin.service';
import {AuthService} from '../../core/services/auth/auth.service';
import {Spvitamin} from '../../model/spvitamin-admin-models';


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
  public settings: { [index: string]: Spvitamin.ServerParameter[] } | null = null;
  public shutdownIsInProgress = false;

  constructor(
    public adminService: AdminService,
    public authService: AuthService
  )
  {

  }

  ngOnInit()
  {
    this.adminService.getSettings().subscribe((data: Spvitamin.ServerSettingsResponse) =>
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

  getKeys(): string[]
  {
    if (this.settings)
    {
      return Object.keys(this.settings);
    }
    return [];
  }

  getSetting(key: string): Spvitamin.ServerParameter[]
  {
    if (this.settings)
    {
      return this.settings[key];
    }
    return [];
  }
}
