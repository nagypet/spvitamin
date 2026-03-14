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

import {Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChange} from '@angular/core';
import {InputBaseComponent} from '../input-base.component';
import {Ngface} from '../../ngface-models';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatOptionModule} from '@angular/material/core';
import {MatSelectModule} from '@angular/material/select';
import {AsyncPipe} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {MatInputModule} from '@angular/material/input';
import {DebounceInputDirective} from '../../directives/debounce-input-directive';
import {A11yModule} from '@angular/cdk/a11y';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {GenericValueSetItem} from '../types';
import {Subscription} from 'rxjs';
import {GenericAutocompleteValueSetProvider} from './generic-autocomplete-value-set-provider';

export interface GenericAutocompleteRequest
{
  widgetId: string;
  searchText: string;
  valueSetProvider: GenericAutocompleteValueSetProvider;
}


export interface GenericAutocompleteValueChangeEvent
{
  widgetId: string;
  value: Ngface.AbstractOption;
}


export interface GenericAutocompleteValueSetChangeEvent
{
  widgetId: string;
  searchText: string;
  valueSet: Ngface.GenericValueSet<Ngface.AbstractOption>;
  selectionChanged: boolean;
}


@Component({
  selector: 'ngface-generic-autocomplete',
  templateUrl: './ngface-generic-autocomplete.component.html',
  imports: [
    MatFormFieldModule,
    MatOptionModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatInputModule,
    AsyncPipe,
    DebounceInputDirective,
    A11yModule,
    ResponsiveClassDirective,
    MatCheckboxModule,
  ],
  standalone: true
})
export class NgfaceGenericAutocompleteComponent extends InputBaseComponent implements OnChanges, OnDestroy
{
  @Input()
  multiselect = false;

  @Output()
  onAutocompleteRequest: EventEmitter<GenericAutocompleteRequest> = new EventEmitter();

  // Only in single-select mode
  @Output()
  onValueChange: EventEmitter<GenericAutocompleteValueChangeEvent> = new EventEmitter();

  // Only in multi-select mode
  @Output()
  onValueSetChange: EventEmitter<GenericAutocompleteValueSetChangeEvent> = new EventEmitter();


  protected valueSetProvider = new GenericAutocompleteValueSetProvider();

  private isValueSetEmpty = true;

  private subscriptions = new Array<Subscription | undefined>();


  /**
   * Az input mezőben megjelenő szöveg az option kiválasztásakor.
   * Itt nem az AbstractOption-t kapod meg, hanem azt, amit a mat-option [value]-jába tettél,
   * jelenleg: valueSetItem.
   */
  displayValueSetItem = (valueSetItem: any): string => {
    // Ha a user még gépel, akkor ez sima string is lehet (Material autocomplete tipikus viselkedés)
    if (typeof valueSetItem === 'string') {
      return valueSetItem;
    }

    const texts: unknown = valueSetItem?.value?.texts;

    if (Array.isArray(texts) && typeof texts[0] === 'string') {
      return texts[0] as string;
    }

    return '';
  };


  constructor()
  {
    super();

    this.subscriptions.push(this.valueSetProvider.valueSet$.subscribe(valueSet =>
    {
      //console.log(`Autocomplete filtered options for ${this.widgetid}: ${values.length}`);
      this.isValueSetEmpty = valueSet?.values.length === 0;

      if (this.multiselect && valueSet)
      {
        this.onValueSetChange.emit({
          widgetId: this.widgetid,
          searchText: this.formControl.value ?? '',
          valueSet: valueSet,
          selectionChanged: false
        });
      }
    }));
  }


  override ngOnChanges(changes: { [propName: string]: SimpleChange }): void
  {
    super.ngOnChanges(changes);
    this.valueSetProvider.valueSet = this.getData().data.extendedReadOnlyData.valueSet;
    this.valueSetProvider.setMultiselect = this.multiselect;
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


  getData(): Ngface.GenericAutocomplete<Ngface.AbstractOption>
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'GenericAutocomplete')
    {
      return {
        type: 'GenericAutocomplete',
        data: {
          type: 'GenericAutocomplete.Data',
          //value: {id: '__id__', type: '', texts: ['alma']},
          value: null,
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
    return this.formdata?.widgets[this.widgetid] as Ngface.GenericAutocomplete<Ngface.AbstractOption>;
  }


  onSearchTextChange($event: string): void
  {
    // I do not remember why this block was here
    // if (this.isValueSetEmpty && !this.multiselect)
    // {
    //   this.onValueChange.emit({widgetId: this.widgetid, value: $event});
    // }

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
    if (!this.multiselect)
    {
      this.onValueChange.emit({widgetId: this.widgetid, value: $event.option.value});
    }
  }


  onClick($event: MouseEvent)
  {
    if (this.valueSetProvider.isRemote())
    {
      //console.log(`Autocomplete request for ${this.widgetid}, search text: ${this.formControl.value}`);
      this.onAutocompleteRequest.emit({
        widgetId: this.widgetid,
        searchText: this.formControl.value,
        valueSetProvider: this.valueSetProvider
      });
    }
  }


  isAnySelected(valueSetItem: GenericValueSetItem): boolean
  {
    if (!valueSetItem.masterSelect)
    {
      return valueSetItem.selected;
    }
    else
    {
      return this.valueSetProvider.isAnySelected();
    }
  }


  isAllSelected(valueSetItem: GenericValueSetItem): boolean
  {
    if (!valueSetItem.masterSelect)
    {
      return valueSetItem.selected;
    }
    else
    {
      return this.valueSetProvider.isAllSelected();
    }
  }


  isCheckBoxEnabled(valueSetItem: GenericValueSetItem): boolean
  {
    if (!valueSetItem.masterSelect)
    {
      return true;
    }
    else
    {
      return !!this.valueSetProvider.filteredOptions$.subscribe(options => options.find(c => !c.masterSelect));
    }
  }


  onCheckBoxClicked(choice: GenericValueSetItem, $event: boolean): void
  {
    this.onItemSelected(choice, $event);
  }


  onItemSelected(choice: GenericValueSetItem, b?: boolean): void
  {
    const newValue = b ?? !choice.selected;
    if (choice.masterSelect)
    {
      this.valueSetProvider.selectAll(newValue);
    }
    else
    {
      this.valueSetProvider.select(choice.id, newValue);
    }

    var valueSet = this.valueSetProvider.valueSet;
    if (this.multiselect && valueSet)
    {
      this.onValueSetChange.emit({
        widgetId: this.widgetid,
        searchText: this.formControl.value ?? '',
        valueSet: valueSet,
        selectionChanged: true
      });
    }
  }
}
