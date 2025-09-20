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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ValueSetProvider} from './value-set-provider';
import {Ngface} from '../../../ngface-models';
import {MatButtonModule} from '@angular/material/button';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatIconModule} from '@angular/material/icon';
import {DebounceInputDirective} from '../../../directives/debounce-input-directive';
import {A11yModule} from '@angular/cdk/a11y';
import {ValueSetItem} from '../../types';


export interface ValueSetSearchEvent
{
  searchText: string;
  valueSetProvider: ValueSetProvider;
}


export interface FilterChangeEvent
{
  filterer?: Ngface.Filterer;
  changed: boolean;
}

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-excel-filter',
  templateUrl: './excel-filter.component.html',
  styleUrls: ['./excel-filter.component.scss'],
  standalone: true,
  imports: [ReactiveFormsModule, A11yModule, DebounceInputDirective, MatIconModule, MatCheckboxModule, FormsModule, MatButtonModule]
})
export class ExcelFilterComponent implements OnInit
{
  formControlSearch: FormControl<string> = new FormControl<string>('', {nonNullable: true});

  @Input()
  filterer?: Ngface.Filterer;

  @Output()
  valueSetSearchEvent: EventEmitter<ValueSetSearchEvent> = new EventEmitter();

  @Output()
  filtererChangeEvent: EventEmitter<FilterChangeEvent> = new EventEmitter();

  // tslint:disable-next-line:no-output-on-prefix
  @Output()
  onCloseEvent: EventEmitter<void> = new EventEmitter();

  valueSetProvider: ValueSetProvider = new ValueSetProvider();


  constructor()
  {
  }


  ngOnInit(): void
  {
    if (this.filterer?.active)
    {
      this.formControlSearch.setValue(this.filterer?.searchText);
      this.valueSetProvider = new ValueSetProvider(this.filterer);
    }
    else
    {
      this.formControlSearch.setValue('');
      this.valueSetProvider = new ValueSetProvider(this.filterer);
      this.valueSetProvider.clearFilter();
      this.reloadValueSetFromServer('');
    }
  }


  onItemSelected(choice: ValueSetItem, b?: boolean): void
  {
    const newValue = b ?? !choice.selected;
    if (choice.masterSelect)
    {
      this.valueSetProvider.selectAll(newValue);
    }
    else
    {
      choice.selected = newValue;
    }
  }


  onCheckBoxClicked(choice: ValueSetItem, $event: boolean): void
  {
    this.onItemSelected(choice, $event);
  }


  onCancel(): void
  {
    this.onCloseEvent.emit();
  }


  onOk(): void
  {
    if (this.filterer)
    {
      const newFilterer = this.getNewFilterer();
      this.filtererChangeEvent.emit({filterer: newFilterer, changed: this.valueSetProvider.isChanged()});
      if (!this.filterer.valueSet.remote)
      {
        this.filterer = newFilterer;
      }
    }
  }


  private getNewFilterer(): Ngface.Filterer | undefined
  {
    if (this.filterer)
    {
      return {
        active: this.filterer.active,
        column: this.filterer.column,
        operator: this.filterer.operator,
        searchText: this.formControlSearch.value ?? '',
        valueSet: {
          values: this.valueSetProvider.valueSetItems.filter(i => !i.masterSelect && i.selectable),
          truncated: this.valueSetProvider.truncated,
          remote: this.filterer.valueSet.remote
        },
        type: this.filterer.type,
        order: this.filterer.order
      };
    }

    return undefined;
  }


  onSearchTextChange($event: string): void
  {
    this.valueSetProvider.searchText = $event;
    this.reloadValueSetFromServer($event);
  }


  isAnySelected(valueSetItem: ValueSetItem): boolean
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


  isAllSelected(valueSetItem: ValueSetItem): boolean
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


  isCheckBoxEnabled(valueSetItem: ValueSetItem): boolean
  {
    if (!valueSetItem.masterSelect)
    {
      return true;
    }
    else
    {
      return !!this.valueSetProvider.getVisibleItems().find(c => !c.masterSelect);
    }
  }


  onClearFilter(): void
  {
    if (this.filterer)
    {
      this.formControlSearch.setValue('');
      this.valueSetProvider.clearFilter();
      this.onOk();
    }
  }


  private reloadValueSetFromServer(searchText: string): void
  {
    if (this.filterer?.valueSet.remote)
    {
      this.valueSetSearchEvent.emit({searchText, valueSetProvider: this.valueSetProvider});
    }
  }
}
