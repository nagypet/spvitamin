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

import {Component, Input, OnInit} from '@angular/core';
import {Ngface} from '../../ngface-models';
import {MatBadgeModule} from '@angular/material/badge';
import {MatTooltipModule} from '@angular/material/tooltip';
import {NgClass} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';

@Component({
    selector: 'ngface-button',
    templateUrl: './ngface-button.component.html',
    standalone: true,
    imports: [MatButtonModule, NgClass, MatTooltipModule, MatBadgeModule, ResponsiveClassDirective]
})
export class NgfaceButtonComponent implements OnInit
{

  @Input()
  formdata?: Ngface.Form;

  @Input()
  widgetid = '';

  @Input()
  enabled = true;

  @Input()
  badge?: string;

  constructor()
  {
  }

  ngOnInit(): void
  {
  }

  getData(): Ngface.Button
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'Button')
    {
      return {
        data: {type: 'VoidWidgetData'},
        style: 'NONE',
        badge: '',
        type: 'Button',
        label: 'undefined button',
        enabled: false,
        id: '',
        hint: ''
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.Button;
  }

  getClass()
  {
    return {
      '': this.getData().style === 'NONE',
      'mat-primary': this.getData().style === 'PRIMARY',
      'mat-accent': this.getData().style === 'ACCENT',
      'mat-warn': this.getData().style === 'WARN'
    };
  }

  getBadge(): string
  {
    if (this.badge)
    {
      return this.badge;
    }

    return this.getData().badge;
  }

  isEnabled(): boolean
  {
    return this.enabled && this.getData().enabled;
  }
}
