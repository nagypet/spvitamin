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

import {AfterViewInit, Component, ElementRef, Input, ViewChild} from '@angular/core';
import {Ngface} from '../../ngface-models';
import {InputBaseComponent} from '../input-base.component';
import {NgIf} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {NgfaceWidgetFactory} from '../ngface-widget-factory';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-text-input',
  templateUrl: './ngface-text-input.component.html',
  standalone: true,
  imports: [MatFormFieldModule, MatInputModule, ReactiveFormsModule, NgIf, ResponsiveClassDirective]
})
export class NgfaceTextInputComponent extends InputBaseComponent implements AfterViewInit
{
  @ViewChild('textInput') textInput!: ElementRef<HTMLInputElement>;

  @Input()
  focused = false;

  constructor()
  {
    super();
  }

  ngAfterViewInit()
  {
    if (this.focused)
    {
      setTimeout(() =>
      {
        this.textInput.nativeElement.focus();
      });
    }
  }

  getData(): Ngface.TextInput
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'TextInput')
    {
      return NgfaceWidgetFactory.createTextInput();
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.TextInput;
  }


  isPassword(): boolean
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'TextInput')
    {
      return false;
    }
    let textInputWidget = this.formdata?.widgets[this.widgetid] as Ngface.TextInput;
    return !!textInputWidget.password;
  }
}
