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

import {NgModule} from '@angular/core';
import {CommonModule, HashLocationStrategy, LocationStrategy} from '@angular/common';
import {LayoutComponent} from './layout/layout.component';
import {HeaderComponent} from './header/header.component';
import {FooterComponent} from './footer/footer.component';
import {SettingsComponent} from './settings/settings.component';
import {CertificatesComponent} from './certificates/certificates.component';
import {RouterModule, Routes} from '@angular/router';
import {AdminService} from '../services/admin.service';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {ToastrModule} from 'ngx-toastr';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {LoginComponent} from './login/login.component';
import {BrowserModule} from '@angular/platform-browser';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {KeystoreComponent, NgbdModalContent} from './certificates/keystore/keystore.component';
import {AboutComponent} from './about/about.component';
import {TabSetComponent} from './tab-set/tab-set.component';
import {AuthGuard} from '../services/auth/auth.guard';
import {AuthService} from '../services/auth/auth.service';
import {FunctionDisabledWarningComponent} from './function-disabled-warning/function-disabled-warning.component';
import {TokenInterceptor} from '../interceptors/token-interceptor';
import {ErrorInterceptor} from '../interceptors/error-interceptor.service';
import {FormsModule} from '@angular/forms';


export const routes: Routes = [
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
  declarations: [
    LayoutComponent,
    HeaderComponent,
    FooterComponent,
    SettingsComponent,
    CertificatesComponent,
    LoginComponent,
    KeystoreComponent,
    NgbdModalContent,
    AboutComponent,
    TabSetComponent,
    FunctionDisabledWarningComponent,
  ],
  imports: [
    CommonModule,
    BrowserModule,
    BrowserAnimationsModule,
    NgbModule,
    RouterModule.forRoot(routes, {relativeLinkResolution: 'legacy'}),
    HttpClientModule,
    ToastrModule.forRoot(),
    FormsModule,
  ],
  providers: [
    AdminService,
    AuthService,
    AuthGuard,
    {provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true},
    {provide: LocationStrategy, useClass: HashLocationStrategy}
  ],
  exports: [LayoutComponent],
  entryComponents: [NgbdModalContent]
  //bootstrap: [LayoutComponent]
})
export class UiModule {
}
