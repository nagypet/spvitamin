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
import {DeviceTypeService} from './core/services/device-type.service';
import {AuthService} from './core/services/auth/auth.service';
import {environment} from '../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  imports: [
    LayoutComponent
  ],
  standalone: true
})
export class AppComponent
{
  title = 'admingui';


  constructor(
    private deviceTypeService: DeviceTypeService,
    private authService: AuthService,
  )
  {
  }


  ngOnInit(): void
  {
    this.authService.getProfile().subscribe();
    this.onWindowResize();
    this.loadStylesheet(`themes/${environment.theme}/microdms-theme.css`);
  }


  @HostListener('window:resize', ['$event'])
  onWindowResize(): void
  {
    this.deviceTypeService.calculateDeviceType();
  }


  loadStylesheet(path: string): void
  {
    const linkEl = document.createElement('link');
    linkEl.rel = 'stylesheet';
    linkEl.href = path;
    document.head.appendChild(linkEl);
  }
}
