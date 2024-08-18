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

import {RouterModule, Routes} from '@angular/router';
import {TabSetComponent} from './ui/tab-set/tab-set.component';
import {SettingsComponent} from './ui/settings/settings.component';
import {CertificatesComponent} from './ui/certificates/certificates.component';
import {AuthGuard} from './services/auth/auth.guard';
import {FunctionDisabledWarningComponent} from './ui/function-disabled-warning/function-disabled-warning.component';
import {LoginComponent} from './ui/login/login.component';
import {AboutComponent} from './ui/about/about.component';
import {NgModule} from '@angular/core';

export const APP_ROUTES: Routes = [
  {path: '', redirectTo: 'admin-gui/settings', pathMatch: 'full'},
  {path: 'admin-gui', redirectTo: 'admin-gui/settings', pathMatch: 'full'},
  {
    path: 'admin-gui', component: TabSetComponent,
    children: [
      {path: 'settings', component: SettingsComponent},
      {path: 'keystore', component: CertificatesComponent, canActivate: [AuthGuard]},
      {path: 'truststore', component: CertificatesComponent, canActivate: [AuthGuard]},
      {path: 'keystore-disabled', component: FunctionDisabledWarningComponent},
      {path: 'truststore-disabled', component: FunctionDisabledWarningComponent},
    ],
  },
  {path: 'admin-gui/login', component: LoginComponent},
  {path: 'admin-gui/about', component: AboutComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(APP_ROUTES, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule
{
}
