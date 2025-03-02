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

import {DataSource} from '@angular/cdk/collections';
import {BehaviorSubject, Observable} from 'rxjs';
import {Ngface} from '../../ngface-models';


/**
 * Data source for the DataTable view. This class should
 * encapsulate all logic for fetching and manipulating the displayed data
 * (including sorting, pagination, and filtering).
 */
export class DataTableDataSource extends DataSource<Ngface.Row<any>>
{
  private dataSubject = new BehaviorSubject<Ngface.Row<any>[]>([]);

  // tslint:disable-next-line:variable-name
  private _paginator: Ngface.Paginator | null = null;
  get paginator(): Ngface.Paginator | null
  {
    return this._paginator;
  }
  set paginator(paginator: Ngface.Paginator | null)
  {
    this._paginator = paginator;
  }

  constructor()
  {
    super();
  }

  /**
   * Connect this data source to the table. The table will only update when
   * the returned stream emits new items.
   * @returns A stream of the items to be rendered.
   */
  connect(): Observable<Ngface.Row<any>[]>
  {
    return this.dataSubject.asObservable();
  }

  /**
   *  Called when the table is being destroyed. Use this function, to clean up
   * any open connections or free any held resources that were set up during connect.
   */
  disconnect(): void
  {
    this.dataSubject.complete();
  }

  setWidgetData(tableData: Ngface.Table<any>): void
  {
    if (tableData)
    {
      this.dataSubject.next(tableData.rows);
      this.paginator = tableData.data.paginator;
    }
  }

  getRows(): Ngface.Row<any>[]
  {
    return this.dataSubject.getValue();
  }
}
