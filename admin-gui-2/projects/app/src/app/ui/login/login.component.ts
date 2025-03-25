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
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../core/services/auth/auth.service';
import {NgfaceFormComponent} from '../../../../../ngface/src/lib/form/ngface-form/ngface-form.component';
import {NgfaceTextInputComponent} from '../../../../../ngface/src/lib/widgets/ngface-text-input/ngface-text-input.component';
import {NgfaceButtonComponent} from '../../../../../ngface/src/lib/widgets/ngface-button/ngface-button.component';
import {ResponsiveClassDirective} from '../../../../../ngface/src/lib/directives/responsive-class-directive';
import {FormBaseComponent} from '../../../../../ngface/src/lib/form/form-base.component';
import {Ngface} from '../../../../../ngface/src/lib/ngface-models';
import {NgfaceWidgetFactory} from '../../../../../ngface/src/lib/widgets/ngface-widget-factory';
import {AuthenticationRepositoryService} from '../../core/services/authentication-repository.service';
import {MatButton} from '@angular/material/button';
import {environment} from '../../../environments/environment';
import {SpvitaminSecurity} from '../../model/spvitamin-security-models';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  imports: [
    NgfaceFormComponent,
    NgfaceTextInputComponent,
    NgfaceButtonComponent,
    ResponsiveClassDirective,
    MatButton,
  ],
  standalone: true
})
export class LoginComponent extends FormBaseComponent implements OnInit
{
  protected errorText?: string;
  protected selectedAuthenticationType?: SpvitaminSecurity.AuthenticationType;

  private returnUrl = '';
  protected authenticationTypes: Array<SpvitaminSecurity.AuthenticationType> = [];


  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private authenticationRepositoryService: AuthenticationRepositoryService
  )
  {
    super();

    // Initializing ngface widgets
    let form = {widgets: {}} as Ngface.Form;
    form.widgets['username'] = this.createTextInputWidget('username', 'Username');
    form.widgets['password'] = this.createTextInputWidget('password', 'Password', true);
    form.widgets['button-login'] = NgfaceWidgetFactory.createButton({id: 'button-login', label: 'Login'});
    form.widgets['button-cancel'] = NgfaceWidgetFactory.createButton({id: 'button-cancel', label: 'Cancel', style: 'NONE'});
    this.formData = form;
  }

  private createTextInputWidget(id: string, label: string, password = false): Ngface.TextInput
  {
    return NgfaceWidgetFactory.createTextInput({id, label, validators: [{type: 'Required', message: `Enter ${label}`}], password});
  }


  ngOnInit()
  {
    this.authenticationRepositoryService.getAuthenticationRepository().subscribe((result: SpvitaminSecurity.AuthenticationRepository) =>
    {
      this.authenticationTypes = result.authenticationTypes;
      if (result.authenticationTypes && result.authenticationTypes.length == 1)
      {
        this.selectedAuthenticationType = result.authenticationTypes[0];
      }
    });
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }


  onLogin()
  {
    this.formGroup.markAllAsTouched();
    if (!this.formGroup.valid)
    {
      console.warn('Data is invalid!');
    }
    else
    {
      const submitData = this.getSubmitData();
      const userName = (submitData['username'] as Ngface.TextInput.Data).value!;
      const password = (submitData['password'] as Ngface.TextInput.Data).value!;

      this.authService.login(userName, password).subscribe(data =>
      {
        this.router.navigateByUrl(this.returnUrl);
      }, error =>
      {
        this.errorText = 'Invalid username or password!';
      });
    }
  }


  onCancel()
  {
    this.authService.logout();
    this.router.navigateByUrl('/');
  }


  onKeyPress(event: KeyboardEvent)
  {
    // console.log("onKeyPress " + event.key);
    if (event.key === 'Enter')
    {
      this.onLogin();
    }
    else if (event.key === 'Escape')
    {
      this.onCancel();
    }
  }


  onAuthenticationSelect(type: SpvitaminSecurity.AuthenticationType)
  {
    console.log(`Selected authentication type: ${type.label}`);
    this.selectedAuthenticationType = type;

    if (type.type === 'oauth2')
    {
      //window.location.href = 'http://localhost:8410/oauth2/authorization/microsoft';
      //window.location.href = 'http://localhost:8410/api/spvitamin/oauth2/authorization?provider=microsoft';
      window.location.href = `${environment.baseURL}/api/spvitamin/oauth2/authorization?provider=${type.provider}`;
    }
  }
}
