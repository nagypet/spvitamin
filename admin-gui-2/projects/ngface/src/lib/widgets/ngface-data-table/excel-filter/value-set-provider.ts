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

import {Ngface} from '../../../ngface-models';
import {ValueSetItem} from '../../types';

export class ValueSetProvider
{
  private _valueSetItems: ValueSetItem[] = [];

  setValueSet(valueSet?: Ngface.ValueSet, searchText?: string)
  {
    this._valueSetItems = [];
    this._truncated = valueSet?.truncated ?? false;

    if (valueSet)
    {
      this._valueSetItems.push({masterSelect: true, text: '(Select All)', selected: true, selectable: true});
      if (valueSet.values)
      {
        valueSet.values.forEach(item => this._valueSetItems.push({
          masterSelect: false,
          text: item.text ?? '(Blanks)',
          selected: item.selected,
          selectable: true
        }));
      }

      if (valueSet.truncated)
      {
        this._valueSetItems.push({
          masterSelect: false,
          text: 'The list is truncated...',
          selected: false,
          selectable: false
        });
      }
    }

    if (searchText)
    {
      this._searchText = searchText;
    }
  }

  get valueSetItems(): ValueSetItem[]
  {
    return this._valueSetItems
      .filter(i => !i.masterSelect && i.selectable)
      .map(i => this.contains(i.text, this._searchText) ?
        {text: i.text, selectable: true, masterSelect: false, selected: i.selected} :
        {text: i.text, selectable: true, masterSelect: false, selected: false});
  }

  public getVisibleItems(): ValueSetItem[]
  {
    return this.applySearchText();
  }


  // searchText
  private _searchText: string | undefined;
  set searchText(value: string | undefined)
  {
    this._searchText = value;
  }

  get searchText(): string | undefined
  {
    return this._searchText;
  }

  private remote = false;
  private _truncated = false;
  get truncated(): boolean
  {
    return this._truncated;
  }

  constructor(filterer?: Ngface.Filterer)
  {
    this.remote = filterer?.valueSet.remote ?? false;
    this._truncated = filterer?.valueSet?.truncated ?? false;
    this.setValueSet(filterer?.valueSet, filterer?.searchText);
  }

  // Filter _criteria by searchText
  private applySearchText(): ValueSetItem[]
  {
    return this._valueSetItems.filter(c => c.masterSelect || this.contains(c.text, this._searchText));
  }

  // returns true if text contains searchTerm
  private contains(text: string, searchTerm: string | undefined): boolean
  {
    if (!searchTerm)
    {
      return true;
    }
    return text.toLowerCase().indexOf(searchTerm.toLowerCase()) >= 0;
  }

  selectAll(b: boolean)
  {
    this._valueSetItems.forEach(c => c.selected = b);
  }


  isAnySelected()
  {
    return !!this.getVisibleItems()
      .filter(r => !r.masterSelect && r.selectable)
      .find(r => r.selected);
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
    return !this.getVisibleItems()
      .filter(r => !r.masterSelect && r.selectable)
      .find(r => !r.selected);
  }

  public clearFilter()
  {
    this._searchText = '';
    this.selectAll(true);
  }

  public isChanged(): boolean
  {
    return !!this._searchText || !this.isAllVisibleSelected();
  }
}
