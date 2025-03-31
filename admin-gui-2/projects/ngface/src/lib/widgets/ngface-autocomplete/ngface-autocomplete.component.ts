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
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatOptionModule} from '@angular/material/core';
import {MatSelectModule} from '@angular/material/select';
import {AsyncPipe, NgForOf, NgIf} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {MatInputModule} from '@angular/material/input';
import {DebounceInputDirective} from '../../directives/debounce-input-directive';
import {A11yModule} from '@angular/cdk/a11y';
import {AutocompleteValueSetProvider} from './autocomplete-value-set-provider';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {colors} from 'ng-packagr/lib/utils/color';

export interface AutocompleteRequest
{
  widgetId: string;
  searchText: string;
  valueSetProvider: AutocompleteValueSetProvider;
}

export interface AutocompleteValueChangeEvent
{
  widgetId: string;
  value: string;
}


@Component({
  selector: 'ngface-autocomplete',
  templateUrl: './ngface-autocomplete.component.html',
  imports: [
    MatFormFieldModule,
    MatOptionModule,
    MatSelectModule,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatInputModule,
    AsyncPipe,
    DebounceInputDirective,
    A11yModule,
    ResponsiveClassDirective
  ],
  standalone: true
})
export class NgfaceAutocompleteComponent extends InputBaseComponent implements OnChanges
{
  @Output()
  onAutocompleteRequest: EventEmitter<AutocompleteRequest> = new EventEmitter();

  @Output()
  onValueChange: EventEmitter<AutocompleteValueChangeEvent> = new EventEmitter();

  protected valueSetProvider = new AutocompleteValueSetProvider();

  private isValueSetEmpty = true;


  constructor()
  {
    super();

    this.valueSetProvider.filteredOptions.subscribe(values =>
    {
      this.isValueSetEmpty = values.length === 0;
      //console.log(`Autocomplete response for ${this.widgetid}:`, values);
    });
  }


  override ngOnChanges(changes: { [propName: string]: SimpleChange }): void
  {
    super.ngOnChanges(changes);
    this.valueSetProvider.valueSet = this.getData().data.extendedReadOnlyData.valueSet;
  }


  getData(): Ngface.Autocomplete
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'Autocomplete')
    {
      return {
        type: 'Autocomplete',
        data: {
          type: 'Autocomplete.Data',
          value: '',
          extendedReadOnlyData: {valueSet: {remote: false, truncated: false, values: []}}
        },
        placeholder: 'widget id: ' + this.widgetid,
        label: 'undefined label',
        validators: [],
        enabled: false,
        id: '',
        hint: ''
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.Autocomplete;
  }


  onSearchTextChange($event: string): void
  {
    if (this.isValueSetEmpty)
    {
      this.onValueChange.emit({widgetId: this.widgetid, value: $event});
    }

    if (this.valueSetProvider.isRemote())
    {
      //console.log(`Autocomplete request for ${this.widgetid}: ${$event}`);
      this.onAutocompleteRequest.emit({widgetId: this.widgetid, searchText: $event, valueSetProvider: this.valueSetProvider});
    }
    else
    {
      this.valueSetProvider.searchText = $event;
    }
  }


  onOptionSelected($event: MatAutocompleteSelectedEvent)
  {
    this.onValueChange.emit({widgetId: this.widgetid, value: $event.option.value});
  }


  onClick($event: MouseEvent)
  {
    if (this.valueSetProvider.isRemote())
    {
      //console.log(`Autocomplete request for ${this.widgetid}: ''`);
      this.onAutocompleteRequest.emit({widgetId: this.widgetid, searchText: '', valueSetProvider: this.valueSetProvider});
    }
  }
}
