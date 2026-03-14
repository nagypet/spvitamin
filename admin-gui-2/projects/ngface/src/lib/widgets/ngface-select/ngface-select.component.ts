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

import {Component, EventEmitter, OnChanges, Output, SimpleChange} from '@angular/core';
import {InputBaseComponent} from '../input-base.component';
import {Ngface} from '../../ngface-models';
import {MatOptionModule} from '@angular/material/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MatSelectChange, MatSelectModule} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';

export interface SelectionChangeEvent
{
  widgetId: string;
  optionId: string;
  optionValue: string;
}


@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-select',
  templateUrl: './ngface-select.component.html',
  standalone: true,
  imports: [MatFormFieldModule, MatSelectModule, ReactiveFormsModule, MatOptionModule, ResponsiveClassDirective]
})
export class NgfaceSelectComponent extends InputBaseComponent implements OnChanges
{

  @Output()
  onSelectionChange: EventEmitter<SelectionChangeEvent> = new EventEmitter();

  constructor()
  {
    super();
  }

  override ngOnChanges(changes: { [propName: string]: SimpleChange }): void
  {
    super.ngOnChanges(changes);
    this.formControl.setValue(this.getData()?.data?.selected);
  }

  getData(): Ngface.Select
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'Select')
    {
      return {
        type: 'Select',
        data: {type: 'Select.Data', options: {}, selected: ''},
        placeholder: '',
        validators: [],
        label: 'undefined label',
        enabled: false,
        id: '',
        hint: ''
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.Select;
  }


  getOptionIds(): string[]
  {
    return Object.keys(this.getData().data?.options);
  }


  getOptionValue(id: string): string | null
  {
    return this.getData().data?.options[id];
  }


  selectionChange($event: MatSelectChange)
  {
    this.onSelectionChange.emit({
      widgetId: this.widgetid,
      optionId: $event.value,
      optionValue: this.getOptionValue($event.value)!
    });
  }
}
