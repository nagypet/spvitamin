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

import {Component, HostListener} from '@angular/core';
import {LayoutComponent} from './ui/layout/layout.component';
import {DeviceTypeService} from './services/device-type.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [
    LayoutComponent
  ],
  standalone: true
})
export class AppComponent {
  title = 'admingui';

  constructor(public deviceTypeService: DeviceTypeService)
  {
  }

  ngOnInit(): void
  {
    this.onWindowResize();
  }


  @HostListener('window:resize', ['$event'])
  onWindowResize(): void
  {
    this.deviceTypeService.calculateDeviceType();
  }
}
