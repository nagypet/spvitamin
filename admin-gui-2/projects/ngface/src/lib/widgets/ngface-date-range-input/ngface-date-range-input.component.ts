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

import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {InputBaseComponent} from '../input-base.component';
import {Ngface} from '../../ngface-models';
import {AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidatorFn} from '@angular/forms';
import {NgIf} from '@angular/common';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {debounceTime, Subscription} from 'rxjs';

export interface DateRangeValueChangeEvent
{
  widgetId: string;
  startDate: Date;
  endDate: Date;
}

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-date-range-input',
  templateUrl: './ngface-date-range-input.component.html',
  standalone: true,
  imports: [MatFormFieldModule, MatDatepickerModule, ReactiveFormsModule, NgIf, ResponsiveClassDirective]
})
export class NgfaceDateRangeInputComponent extends InputBaseComponent implements OnInit, OnDestroy, OnChanges
{
  @Output()
  onValueChange: EventEmitter<DateRangeValueChangeEvent> = new EventEmitter();

  private lastEmittedEvent: DateRangeValueChangeEvent | undefined;

  // tslint:disable-next-line:variable-name
  private _range: FormGroup<any> = new FormGroup({
    start: new FormControl<Date>(new Date()),
    end: new FormControl<Date>(new Date()),
  });
  get range(): FormGroup<any>
  {
    return this._range;
  }

  override get formGroupItem(): AbstractControl
  {
    return this.range;
  }

  // Misused here to generate a getter in the web-component
  @Input()
  // tslint:disable-next-line:variable-name
  protected override get_form_group_item: AbstractControl = this.formGroupItem;

  private subscriptions = new Array<Subscription | undefined>();


  constructor()
  {
    super();
  }

  ngOnInit(): void
  {
    this.subscriptions.push(this.range.valueChanges
      .pipe(debounceTime(1000))
      .subscribe(value =>
      {
        if (this.range.valid)
        {
          let event = {widgetId: this.widgetid, startDate: value.start, endDate: value.end};
          if (this.lastEmittedEvent?.startDate !== event.startDate || this.lastEmittedEvent?.endDate !== event.endDate)
          {
            this.lastEmittedEvent = event;
            this.onValueChange.emit(event);
          }
        }
      }));
  }


  ngOnDestroy(): void
  {
    this.subscriptions.forEach(i =>
    {
      if (i)
      {
        i.unsubscribe();
      }
    });
  }


  override ngOnChanges(): void
  {
    const startDate = this.getData()?.data?.startDate ? this.getData()?.data?.startDate : '';
    const endDate = this.getData()?.data?.endDate ? this.getData()?.data?.endDate : '';

    const currentRangeValue = this.range.value;
    if (currentRangeValue?.start !== startDate || currentRangeValue?.end !== endDate)
    {
      this.range.setValue({start: startDate, end: endDate}, {emitEvent: false});
    }
    this.lastEmittedEvent = {widgetId: this.widgetid, startDate: this.range.value.start, endDate: this.range.value.end};
    // Validators for startDate
    const startDateValidators = new Array<ValidatorFn>();
    this.getData()?.validators?.forEach(v =>
    {
      this.createNgValidators(v).forEach(ngValidator => startDateValidators.push(ngValidator));
    });
    this.range.controls['start']?.setValidators(startDateValidators);

    // Validators for endDate
    const endDateValidators = new Array<ValidatorFn>();
    this.getData()?.validators?.forEach(v =>
    {
      this.createNgValidators(v).forEach(ngValidator => endDateValidators.push(ngValidator));
    });
    this.range.controls['end']?.setValidators(endDateValidators);

    this.getData().enabled ? this.range.enable() : this.range.disable();
  }


  getData(): Ngface.DateRangeInput
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'DateRangeInput')
    {
      return {
        type: 'DateRangeInput',
        data: {type: 'DateRangeInput.Data', startDate: new Date(), endDate: new Date()},
        placeholder: '',
        validators: [],
        placeholder2: '',
        validators2: [],
        label: 'undefined label',
        enabled: false,
        id: '',
        hint: ''
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.DateRangeInput;
  }


  override getValidationErrors(): string
  {
    const validationErrorsStart = this.getValidationErrorsFromFormControl(this.range.controls['start'], this.getData()?.validators);
    const validationErrorsEnd = this.getValidationErrorsFromFormControl(this.range.controls['end'], this.getData()?.validators2);

    const validationErrors = validationErrorsStart.concat(validationErrorsEnd);
    return validationErrors?.join(' ');
  }
}
