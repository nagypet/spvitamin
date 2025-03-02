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

import {FormControl, FormGroupDirective, NgForm} from '@angular/forms';
import {Component, Input} from '@angular/core';
import {ErrorStateMatcher} from '@angular/material/core';
import {Ngface} from '../ngface-models';

/** Error when invalid control is dirty, touched, or submitted. */
export class MyErrorStateMatcher implements ErrorStateMatcher
{
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean
  {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}


@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-custom-widet-base',
  template: ''
})
export abstract class CustomWidgetBaseComponent
{
  @Input()
  formdata?: Ngface.Form;

  @Input()
  widgetid = '';

  // tslint:disable-next-line:variable-name
  private _floatLabelControl = new FormControl('auto');
  get floatLabelControl(): FormControl
  {
    return this._floatLabelControl;
  }

  errorStateMatcher = new MyErrorStateMatcher();

  protected constructor()
  {
  }


  abstract getData(): Ngface.Widget<any, any>;
}
