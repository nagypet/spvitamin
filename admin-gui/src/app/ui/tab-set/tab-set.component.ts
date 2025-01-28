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

import {Component, OnInit} from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {NgForOf} from '@angular/common';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {MatTabsModule} from '@angular/material/tabs';

export interface TabDef
{
  route: string;
  title: string;
}

@Component({
  selector: 'app-tab-set',
  templateUrl: './tab-set.component.html',
  styleUrls: ['./tab-set.component.scss'],
  imports: [
    NgForOf,
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatTabsModule
  ],
  standalone: true
})
export class TabSetComponent implements OnInit
{

  tabs: Array<TabDef> = [];

  constructor(
    private adminService: AdminService,
    public router: Router
  )
  {
  }

  ngOnInit()
  {
    this.adminService.getVersionInfo().subscribe(data =>
    {
      const keystoreAdminEnabled = data.KeystoreAdminEnabled;
      if (keystoreAdminEnabled === 'true')
      {
        this.tabs.push({route: 'admin-gui/settings', title: 'Settings'});
        this.tabs.push({route: 'admin-gui/keystore', title: 'Keystore'});
        this.tabs.push({route: 'admin-gui/truststore', title: 'Truststore'});
      }
      else
      {
        this.tabs.push({route: 'admin-gui/settings', title: 'Settings'});
        this.tabs.push({route: 'admin-gui/keystore-disabled', title: 'Keystore'});
        this.tabs.push({route: 'admin-gui/truststore-disabled', title: 'Truststore'});
      }
    });
  }

  isActive(tab)
  {
    return `/${tab.route}` === this.router.url;
  }
}
