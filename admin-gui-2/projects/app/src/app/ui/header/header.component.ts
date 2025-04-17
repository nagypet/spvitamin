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
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import {NgIf} from '@angular/common';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatBadge} from '@angular/material/badge';
import {AdminService} from '../../core/services/admin.service';
import {AuthService} from '../../core/services/auth/auth.service';
import {NgfaceButtonComponent} from '../../../../../ngface/src/lib/widgets/ngface-button/ngface-button.component';
import {FormBaseComponent} from '../../../../../ngface/src/lib/form/form-base.component';
import {Ngface} from '../../../../../ngface/src/lib/ngface-models';
import {NgfaceWidgetFactory} from '../../../../../ngface/src/lib/widgets/ngface-widget-factory';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  imports: [
    NgIf,
    RouterLink,
    MatToolbar,
    RouterLinkActive,
    MatIcon,
    MatIconButton,
    MatButton,
    MatBadge,
    NgfaceButtonComponent
  ],
  standalone: true
})
export class HeaderComponent  extends FormBaseComponent implements OnInit
{
  public title = '';
  public version = '';

  constructor
  (
    private adminService: AdminService,
    public authService: AuthService,
    private router: Router,
  )
  {
    super();

    // Initializing ngface widgets
    let form = {widgets: {}} as Ngface.Form;
    form.widgets['button-login'] = NgfaceWidgetFactory.createButton({id: 'button-login', label: 'Login', style:'NONE'});
    form.widgets['button-logout'] = NgfaceWidgetFactory.createButton({id: 'button-logout', label: 'Logout', style:'NONE'});
    form.widgets['button-about'] = NgfaceWidgetFactory.createButton({id: 'button-about', label: 'About', style:'PRIMARY'});
    this.formData = form;
  }

  ngOnInit()
  {
    this.adminService.getVersionInfo().subscribe(data =>
    {
      console.log(data);
      this.title = data.Title;
      this.version = data.Version;
    });
  }

  onLogout()
  {
    this.authService.logout().subscribe(() => this.router.navigateByUrl('/'));
  }
}
