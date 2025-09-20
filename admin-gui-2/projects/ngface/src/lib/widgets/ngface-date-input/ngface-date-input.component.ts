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

import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {InputBaseComponent} from '../input-base.component';
import {Ngface} from '../../ngface-models';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {debounceTime, Subscription} from 'rxjs';

export interface DateValueChangeEvent
{
  widgetId: string;
  value: Date;
}

@Component({
  selector: 'ngface-date-input',
  templateUrl: './ngface-date-input.component.html',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    ReactiveFormsModule,
    ResponsiveClassDirective
  ]
})
export class NgfaceDateInputComponent extends InputBaseComponent implements OnInit
{
  @Output()
  onValueChange: EventEmitter<DateValueChangeEvent> = new EventEmitter();

  private lastEmittedEvent: DateValueChangeEvent | undefined;

  private subscriptions = new Array<Subscription | undefined>();


  constructor()
  {
    super();
  }


  ngOnInit(): void
  {
    this.subscriptions.push(this.formControl.valueChanges
      .pipe(debounceTime(1000))
      .subscribe(value =>
      {
        if (this.formControl.valid)
        {
          let event = {widgetId: this.widgetid, value: value} as DateValueChangeEvent;
          if (this.lastEmittedEvent?.value !== event.value)
          {
            this.lastEmittedEvent = event;
            this.onValueChange.emit(event);
          }
        }
      }));
  }


  getData(): Ngface.DateInput
  {
    let widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'DateInput')
    {
      return {
        type: 'DateInput',
        data: {type: 'DateInput.Data', value: new Date()},
        placeholder: 'widget id: ' + this.widgetid,
        label: 'undefined label',
        validators: [],
        enabled: false,
        id: '',
        hint: ''
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.DateInput;
  }
}
