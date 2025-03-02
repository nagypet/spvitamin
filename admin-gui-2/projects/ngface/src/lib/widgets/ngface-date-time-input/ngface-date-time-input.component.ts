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

import {Component, OnChanges, SimpleChange} from '@angular/core';
import {InputBaseComponent} from '../input-base.component';
import {Ngface} from '../../ngface-models';
import {AbstractControl, FormControl, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn} from '@angular/forms';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatError, MatFormFieldModule, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput, MatInputModule} from '@angular/material/input';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {MatFormField} from '@angular/material/select';

export function timeFormatValidator(): ValidatorFn
{
  return (control: AbstractControl): ValidationErrors | null =>
  {
    const value = control.value;

    if (!value)
    {
      return null;
    }

    // Regex a `HH:mm` vagy `HH:mm:ss` formátumhoz
    const isValidTime = /^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/.test(value);

    return isValidTime ? null : {invalidTimeFormat: {value}};
  };
}


@Component({
  selector: 'ngface-date-time-input',
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatError,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatSuffix,
    ResponsiveClassDirective,
    ReactiveFormsModule
  ],
  templateUrl: './ngface-date-time-input.component.html',
  styleUrl: './ngface-date-time-input.component.scss'
})
export class NgfaceDateTimeInputComponent extends InputBaseComponent implements OnChanges
{

  private _timeControl = new FormControl('', [timeFormatValidator()]);
  get timeControl(): FormControl
  {
    return this._timeControl;
  }

  constructor()
  {
    super();

    // Action on time control change
    this._timeControl.valueChanges.subscribe((timeValue) =>
    {
      if (this._timeControl.valid)
      {
        this.onTimeControlChange(timeValue);
      }
    });

    // Action on date control change
    this.formControl.valueChanges.subscribe((dateValue) =>
    {
      if (dateValue)
      {
        this.onDateControlChange(dateValue);
      }
    });
  }


  override ngOnChanges(changes: { [p: string]: SimpleChange })
  {
    super.ngOnChanges(changes);
    const value = this.formControl.value;

    try
    {
      const date = this.parseDate(value);
      if (date)
      {
        const timeString = this.formatTime(date);
        this._timeControl.setValue(timeString);
      }
      else
      {
        this._timeControl.setValue(null);
      }
    }
    catch (error)
    {
      console.error('Error processing date value:', error, value);
    }
  }


  // A fő formControl értékének frissítése a dátum és idő kombinációjával
  private onTimeControlChange(timeValue: string | null): void
  {
    const dateValue = this.formControl.value;

    if (dateValue)
    {
      const date = this.parseDate(dateValue);

      if (date && timeValue)
      {
        const [hours, minutes, seconds = 0] = timeValue.split(':').map(Number);

        // Kijavítjuk a helyi időt
        date.setHours(hours);
        date.setMinutes(minutes);
        date.setSeconds(seconds);

        this.formControl.setValue(date, {emitEvent: false});
        this.formControl.markAsDirty();
      }
    }
  }


  // A dátum frissítése a timeControl értékével
  private onDateControlChange(dateValue: any): void
  {
    const date = this.parseDate(dateValue);

    if (date)
    {
      const currentTimeValue = this._timeControl.value;
      // if (currentTimeValue)
      // {
      //   const [hours, minutes, seconds = 0] = currentTimeValue
      //     ? currentTimeValue.split(':').map(Number)
      //     : [0, 0, 0];
      //
      //   // Kijavítjuk az időt, hogy ne legyen eltérés
      //   date.setHours(hours);
      //   date.setMinutes(minutes);
      //   date.setSeconds(seconds);
      // }
      // else
      // {
        const timeString = this.formatTime(date);
        this._timeControl.setValue(timeString);
      //}

      this.formControl.setValue(date, {emitEvent: false});
      this.formControl.markAsDirty();
    }
  }

  // Segédfüggvény dátum elemzésére
  private parseDate(value: any): Date | null
  {
    if (value instanceof Date)
    {
      return value;
    }
    else if (typeof value === 'string')
    {
      const date = new Date(value);
      return isNaN(date.getTime()) ? null : date;
    }
    return null;
  }

  // Segédfüggvény idő formázására
  private formatTime(date: Date): string
  {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
  }

  getData(): Ngface.DateTimeInput
  {
    let widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'DateTimeInput')
    {
      return {
        type: 'DateTimeInput',
        data: {type: 'DateTimeInput.Data', value: null},
        placeholder: 'widget id: ' + this.widgetid,
        label: 'undefined label',
        validators: [],
        enabled: false,
        id: '',
        hint: ''
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.DateTimeInput;
  }

}
