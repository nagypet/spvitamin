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

import {Component, OnInit} from '@angular/core';
import {AdminService} from '../../services/admin.service';

@Component({
  selector: 'app-tab-set',
  templateUrl: './tab-set.component.html',
  styleUrls: ['./tab-set.component.scss']
})
export class TabSetComponent implements OnInit
{

  tabs: Array<{ route: string, title: string }> = [
    {route: 'settings', title: 'Settings'},
    {route: 'keystore-disabled', title: 'Keystore'},
    {route: 'truststore-disabled', title: 'Truststore'}
  ];

  constructor(
    private adminService: AdminService
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
        this.tabs = [
          {route: 'settings', title: 'Settings'},
          {route: 'keystore', title: 'Keystore'},
          {route: 'truststore', title: 'Truststore'}
        ];
      }
    });
  }
}
