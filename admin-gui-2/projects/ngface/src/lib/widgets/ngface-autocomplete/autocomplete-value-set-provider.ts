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

import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {Ngface} from '../../ngface-models';
import {ValueSetItem} from '../types';
import {map} from 'rxjs/operators';
import {DistinctBehaviorSubject} from './distinct-behavior-subject';


export class AutocompleteValueSetProvider
{
  private _valueSetItems: Ngface.ValueSet.Item[] = [];
  private _searchText: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private _multiselect: boolean = false;
  private _searchTextSubscription?: Subscription;


  set setMultiselect(value: boolean)
  {
    this._multiselect = value;
  }


  private _remote = false;


  public isRemote(): boolean
  {
    return this._remote;
  }


  private _truncated = false;


  // BehaviorSubject for filteredOptions: this is what we see in the list
  private _filteredOptions$: DistinctBehaviorSubject<ValueSetItem[]> = new DistinctBehaviorSubject<ValueSetItem[]>([],
    (a, b) => JSON.stringify(a) === JSON.stringify(b));
  get filteredOptions$(): Observable<ValueSetItem[]>
  {
    return this._filteredOptions$.asObservable();
  }


  // This is the filtered ValueSet for send to other components
  private _valueSet$: DistinctBehaviorSubject<Ngface.ValueSet | undefined> = new DistinctBehaviorSubject<Ngface.ValueSet | undefined>(undefined,
    (a, b) => JSON.stringify(a) === JSON.stringify(b));
  get valueSet$(): Observable<Ngface.ValueSet | undefined>
  {
    return this._valueSet$.asObservable();
  }


  private valueSetNext(items: Ngface.ValueSet.Item[])
  {
    this._valueSet$.next({remote: this._remote, truncated: this._truncated, values: items});
    this._filteredOptions$.next(this.addMasterSelect(items ?? [], this._truncated ?? false));
  }


  set valueSet(valueSet: Ngface.ValueSet)
  {
    this._valueSetItems = valueSet.values ?? [];
    this._truncated = valueSet?.truncated ?? false;
    this._remote = valueSet?.remote ?? false;

    if (!valueSet.remote)
    {
      if (!this._searchTextSubscription)
      {
        this._searchTextSubscription = this._searchText.pipe(
          map(i => this.filter(i || ''))
        ).subscribe(filtered =>
        {
          this.valueSetNext(filtered ?? []);
        });
      }
      let items = this.filter(this._searchText.value);
      this.valueSetNext(items);
    }
    else
    {
      this.valueSetNext(valueSet.values ?? []);
    }
  }


  private addMasterSelect(items: Ngface.ValueSet.Item[], truncated: boolean): ValueSetItem[]
  {
    var valueSetItems: ValueSetItem[] = [];
    // Master select
    if (this._multiselect)
    {
      valueSetItems.push({masterSelect: true, text: '(Select All)', selected: true, selectable: true});
    }

    // Items
    items.forEach(item => valueSetItems.push(({
      masterSelect: false,
      text: item.text ?? '(Blanks)',
      selected: item.selected,
      selectable: true
    })));

    // Truncated indicator
    if (truncated && this._multiselect)
    {
      valueSetItems.push({
        masterSelect: false,
        text: 'The list is truncated...',
        selected: false,
        selectable: false
      });
    }
    return valueSetItems;
  }


  get valueSet(): Ngface.ValueSet | undefined
  {
    return this._valueSet$.value;
  }


  set searchText(value: string)
  {
    this._searchText.next(value);
  }


  private filter(searchText: string): Ngface.ValueSet.Item[]
  {
    const filterValue = searchText.toLowerCase();
    if (!this._valueSetItems)
    {
      return [];
    }
    return this._valueSetItems
      .filter(item => item.text.toLowerCase().includes(filterValue));
  }


  selectAll(value: boolean)
  {
    var items = this._valueSet$.value?.values;
    if (items)
    {
      items.forEach(c => c.selected = value);
      // since we are changing the values of the BehaviorSubject, the valueSetNext will not emit any change on the _valueSet$, which is
      // the intended behavior.
      this.valueSetNext(items);
    }
  }


  select(text: string, value: boolean)
  {
    var items = this._valueSet$.value?.values;
    if (items)
    {
      items.filter(c => c.text === text).forEach(c => c.selected = value);
      // since we are changing the values of the BehaviorSubject, the valueSetNext will not emit any change on the _valueSet$, which is
      // the intended behavior.
      this.valueSetNext(items);
    }
  }


  isAnySelected()
  {
    var items = this._valueSet$.value?.values;
    return !!items?.find(r => r.selected);
  }


  isAllSelected()
  {
    if (this._truncated)
    {
      return false;
    }

    return this.isAllVisibleSelected();
  }


  isAllVisibleSelected()
  {
    var items = this._valueSet$.value?.values;
    return !items?.find(r => !r.selected);
  }
}
