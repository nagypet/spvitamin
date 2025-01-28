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

import { Component, OnInit } from '@angular/core';
import {AdminService} from '../../services/admin.service';
import {HeaderComponent} from '../header/header.component';
import {RouterOutlet} from '@angular/router';
import {FooterComponent} from '../footer/footer.component';
import {TabSetComponent} from '../tab-set/tab-set.component';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
  imports: [
    HeaderComponent,
    RouterOutlet,
    FooterComponent,
    TabSetComponent
  ],
  standalone: true
})
export class LayoutComponent implements OnInit
{
  public version: string = '';
  public build: string = '';

  constructor(
    public adminService: AdminService,
  ) { }

  ngOnInit()
  {
    this.adminService.getVersionInfo().subscribe(data  =>
    {
      console.log(data);
      this.version = data.Version;
      this.build = data.Build;
    });
  }


}
