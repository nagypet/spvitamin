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

import {Component, EventEmitter, Inject, Input, LOCALE_ID, OnDestroy, OnInit, Output} from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  FormGroupDirective,
  FormsModule,
  NG_VALUE_ACCESSOR,
  NgForm,
  ReactiveFormsModule
} from '@angular/forms';
import {NumericFormatter} from '../../../numeric-formatter';
import {getLocaleNumberSymbol, NumberSymbol} from '@angular/common';
import {ErrorStateMatcher} from '@angular/material/core';
import {NumericInputFilterDirective} from '../../../directives/numeric-input-filter-directive';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {ResponsiveClassDirective} from '../../../directives/responsive-class-directive';
import {DateRangeValueChangeEvent} from '../../ngface-date-range-input/ngface-date-range-input.component';
import {debounceTime, Subject} from 'rxjs';

/** Error when invalid control is dirty, touched, or submitted. */
export class MyErrorStateMatcher implements ErrorStateMatcher
{
  private component: IntlNumericInputComponent;

  constructor(component: IntlNumericInputComponent)
  {
    this.component = component;
  }

  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean
  {
    return this.component.validationErrors !== null;
  }
}


@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-intl-numeric-input',
  templateUrl: './intl-numeric-input.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: IntlNumericInputComponent
    }
  ],
  standalone: true,
  imports: [
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    NumericInputFilterDirective,
    FormsModule,
    ResponsiveClassDirective]
})
export class IntlNumericInputComponent implements ControlValueAccessor, OnInit, OnDestroy
{
  @Input()
  set precision(precision: number)
  {
    this._precision = precision;
    this.rewriteValue();
  }

  get precision(): number
  {
    return this._precision;
  }


  get digitGrouping(): boolean
  {
    return this._digitGrouping;
  }

  @Input()
  set digitGrouping(value: boolean)
  {
    this._digitGrouping = value;
  }

  constructor(@Inject(LOCALE_ID) public locale: string)
  {
  }

  @Input()
  label = '';

  @Input()
  placeholder = '';

  @Input()
  hint = '';

  @Input()
  required = false;

  // tslint:disable-next-line:variable-name
  private _precision = 0;
  // tslint:disable-next-line:variable-name
  private _digitGrouping = true;

  @Input()
  prefix = '';

  @Input()
  suffix = '';

  @Input()
  validationErrors: string | null = null;

  @Output()
  onValueChange: EventEmitter<number> = new EventEmitter();

  valueText = '';
  value?: number;

  valueChangeSubject: Subject<number | undefined> = new Subject<number | undefined>();

  ngOnInit(): void
  {
    // A Subject-et debounceTime-mal figyeljük
    this.valueChangeSubject.pipe(
      debounceTime(1000)
    ).subscribe(value =>
    {
      this.onValueChange.emit(value);
    });
  }

  ngOnDestroy(): void
  {
    // A Subject erőforrás-felszabadítása
    this.valueChangeSubject.complete();
  }


  // tslint:disable-next-line:variable-name
  private _floatLabelControl = new FormControl('auto');
  get floatLabelControl(): FormControl
  {
    return this._floatLabelControl;
  }

  errorStateMatcher = new MyErrorStateMatcher(this);

  touched = false;
  disabled = false;

  onChangeFn = (value?: number) =>
  {
  };

  onTouchedFn = () =>
  {
  };

  registerOnChange(onChange: any): void
  {
    this.onChangeFn = onChange;
  }

  registerOnTouched(onTouched: any): void
  {
    this.onTouchedFn = onTouched;
  }


  /**
   * Converting ISO => intl
   */
  writeValue(value?: number): void
  {
    //console.log('writeValue: ' + value);
    this.rewriteValue(value);
    this.value = value;
  }


  rewriteValue(value?: number): void
  {
    let v: number | undefined;
    if (value !== undefined)
    {
      v = value;
    }
    else
    {
      v = this.value;
    }

    if (v && !isNaN(v))
    {
      this.valueText = NumericFormatter.getFormattedValueAsText(v, this.precision, this.digitGrouping, this.locale);
    }
    else
    {
      this.valueText = '';
    }
  }


  markAsTouched(): void
  {
    if (!this.touched)
    {
      this.onTouchedFn();
      this.touched = true;
    }
  }

  setDisabledState(disabled: boolean): void
  {
    this.disabled = disabled;
  }


  /**
   * Converting intl => ISO
   */
  onChange(): void
  {
    //console.log('onChange valueText: ' + this.valueText);
    this.markAsTouched();
    if (!this.disabled)
    {
      const localeSpecificValue: string = this.valueText;
      //console.log('localeSpecificValue: ' + localeSpecificValue);

      // Now remove digit grouping symbol and replace locale specific comma with dot
      const digitGroupingSymbol = getLocaleNumberSymbol(this.locale, NumberSymbol.Group);
      const decimalSymbol = getLocaleNumberSymbol(this.locale, NumberSymbol.Decimal);
      //console.log(`digitGroupingSymbol: ${digitGroupingSymbol}, decimalSymbol: ${decimalSymbol}`);
      const valueString = this.replaceAll(localeSpecificValue, digitGroupingSymbol, '').replace(decimalSymbol, '.');
      //console.log('valueString: ' + valueString);

      const parsedValue = parseFloat(valueString);
      //console.log('parsedValue: ' + parsedValue);

      if (!isNaN(parsedValue))
      {
        this.value = parsedValue;
        this.onChangeFn(parsedValue);
      }
      else
      {
        this.value = undefined;
        this.onChangeFn(undefined);
      }
      this.valueChangeSubject.next(this.value);
    }
  }

  escapeRegExp(str: string): string
  {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& means the whole matched string
  }

  replaceAll(str: string, find: string, replace: string): string
  {
    return str.replace(new RegExp(this.escapeRegExp(find), 'g'), replace);
  }


  /**
   * Formatting model value to intl format
   */
  onBlur(): void
  {
    if (!this.valueText)
    {
      return;
    }

    this.rewriteValue();
  }
}
