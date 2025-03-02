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

import {Ngface} from '../../ngface-models';
import {map, Observable, startWith, Subject} from 'rxjs';

export class AutocompleteValueSetProvider
{
    // valueSet
    // tslint:disable-next-line:variable-name
    private _valueSet?: Ngface.ValueSet;

    set valueSet(value: Ngface.ValueSet)
    {
        this._valueSet = value;
        if (!value.remote)
        {
            this._filteredOptions = this._searchText.pipe(
                startWith(''),
                map(i => this.filter(i || ''))
            );
        }
        else
        {
            const items = value.values.map(i => i.text);
            if (value.truncated)
            {
                items.push('...');
            }
            this._filteredOptions = new Observable<string[]>(observer =>
            {
                observer.next(items);
                observer.complete();
            });
        }
    }

    // searchText
    // tslint:disable-next-line:variable-name
    private _searchText: Subject<string> = new Subject<string>();

    set searchText(value: string)
    {
        this._searchText.next(value);
    }

    // filteredOptions
    // tslint:disable-next-line:variable-name
    private _filteredOptions: Observable<string[]> = new Observable<string[]>();
    get filteredOptions(): Observable<string[]>
    {
        return this._filteredOptions;
    }

    private filter(value: string): string[]
    {
        const filterValue = value.toLowerCase();

        if (!this._valueSet)
        {
            return [];
        }

        return this._valueSet.values
            .map(i => i.text)
            .filter(item => item.toLowerCase().includes(filterValue));
    }

    public isRemote(): boolean
    {
        return !!this._valueSet?.remote;
    }
}
