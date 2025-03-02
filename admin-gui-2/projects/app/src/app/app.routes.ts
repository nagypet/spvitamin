import { Routes } from '@angular/router';
import {SettingsComponent} from './ui/settings/settings.component';
import {CertificatesComponent} from './ui/certificates/certificates.component';
import {FunctionDisabledWarningComponent} from './ui/function-disabled-warning/function-disabled-warning.component';
import {LoginComponent} from './ui/login/login.component';
import {AboutComponent} from './ui/about/about.component';
import {AuthGuard} from './core/services/auth/auth.guard';

export const routes: Routes = [
  {path: '', redirectTo: 'admin-gui/settings', pathMatch: 'full'},
  {path: 'admin-gui', redirectTo: 'admin-gui/settings', pathMatch: 'full'},
  {path: 'admin-gui/settings', component: SettingsComponent},
  {path: 'admin-gui/keystore', component: CertificatesComponent, canActivate: [AuthGuard]},
  {path: 'admin-gui/truststore', component: CertificatesComponent, canActivate: [AuthGuard]},
  {path: 'admin-gui/keystore-disabled', component: FunctionDisabledWarningComponent},
  {path: 'admin-gui/truststore-disabled', component: FunctionDisabledWarningComponent},
  {path: 'admin-gui/login', component: LoginComponent},
  {path: 'admin-gui/about', component: AboutComponent},
];
