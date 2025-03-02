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
import {MatToolbar} from '@angular/material/toolbar';
import {AdminService} from '../../core/services/admin.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
  imports: [
    MatToolbar
  ],
  standalone: true
})
export class FooterComponent implements OnInit
{
  public copyright = '';

  constructor(
    private adminService: AdminService
  )
  {}

  ngOnInit()
  {
    this.adminService.getVersionInfo().subscribe(data =>
    {
      console.log(data);
      this.copyright = data.Copyright;
    });
  }
}
