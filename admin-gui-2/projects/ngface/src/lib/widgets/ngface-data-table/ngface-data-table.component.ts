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

import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  LOCALE_ID,
  OnChanges,
  OnDestroy,
  Output,
  ViewChild
} from '@angular/core';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatSort, MatSortModule, SortDirection} from '@angular/material/sort';
import {MatTable, MatTableModule} from '@angular/material/table';
import {DataTableDataSource} from './data-table-datasource';
import {tap} from 'rxjs/operators';
import {merge, Subscription} from 'rxjs';
import {MatCheckboxChange, MatCheckboxModule} from '@angular/material/checkbox';
import {NumericFormatter} from '../../numeric-formatter';
import {ValueSetSearchEvent} from './excel-filter/excel-filter.component';
import {Ngface} from '../../ngface-models';
import {SafeHtmlPipe} from '../../directives/safe-html.pipe';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';
import {SortFilterHeaderComponent} from './sort-filter-header/sort-filter-header.component';
import {NgScrollbarModule} from 'ngx-scrollbar';
import {NgClass, NgFor, NgIf} from '@angular/common';
import ActionCell = Ngface.ActionCell;
import NumericCell = Ngface.NumericCell;
import DataRetrievalParams = Ngface.DataRetrievalParams;
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';

export interface TableReloadEvent
{
  page: Ngface.DataRetrievalParams.Page;
  sort: Ngface.DataRetrievalParams.Sort;
  filters: Ngface.DataRetrievalParams.Filter[];
}

export interface TableViewParamsChangeEvent
{
  paginator: Ngface.Paginator | null;
  sorter: Ngface.Sorter;
  filtererMap: { [index: string]: Ngface.Filterer };
}

export interface ActionClickEvent<T>
{
  row: Ngface.Row<T>;
  actionId: string;
}

export interface TableValueSetSearchEvent
{
  column: string;
  searchEvent: ValueSetSearchEvent;
}

export interface TableMasterToggleEvent
{
  checked: boolean;
}


@Component({
  selector: 'ngface-data-table',
  templateUrl: './ngface-data-table.component.html',
  styleUrls: ['./ngface-data-table.component.scss'],
  standalone: true,
  imports: [NgClass, NgIf, NgScrollbarModule, MatTableModule, MatSortModule, NgFor, MatCheckboxModule, SortFilterHeaderComponent, MatTooltipModule, MatButtonModule, MatIconModule, MatPaginatorModule, SafeHtmlPipe, ResponsiveClassDirective]
})
export class NgfaceDataTableComponent implements OnDestroy, OnChanges, AfterViewInit
{
  @Input()
  formdata?: Ngface.Form;

  @Input()
  widgetid = '';

  @Input()
  heightpx = 300;

  @Output()
  tableReloadEvent: EventEmitter<TableReloadEvent> = new EventEmitter();

  @Output()
  rowClickEvent: EventEmitter<Ngface.Row<any>> = new EventEmitter();

  @Output()
  actionClickEvent: EventEmitter<ActionClickEvent<any>> = new EventEmitter();

  @Output()
  tableValueSetSearchEvent: EventEmitter<TableValueSetSearchEvent> = new EventEmitter();

  @Output()
  tableViewParamsChangeEvent: EventEmitter<TableViewParamsChangeEvent> = new EventEmitter();

  @Output()
  masterToggleEvent: EventEmitter<TableMasterToggleEvent> = new EventEmitter();

  @ViewChild(MatPaginator) matPaginator!: MatPaginator;
  @ViewChild(MatSort) matSort!: MatSort;
  @ViewChild(MatTable) matTable!: MatTable<any>;
  dataSource: DataTableDataSource = new DataTableDataSource();

  activeFilterer: { [index: string]: Ngface.Filterer } = {};

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayedColumns: string[] = [];
  columnGroups: string[] = [];

  private subscriptions = new Array<Subscription | undefined>();

  constructor(@Inject(LOCALE_ID) public locale: string,
              private el: ElementRef)
  {
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


  ngOnChanges(): void
  {
    const data: Ngface.Table<any> = this.getData();
    this.dataSource.setWidgetData(data);

    this.displayedColumns = [];
    if (data.selectMode === 'CHECKBOX')
    {
      this.displayedColumns = ['___checkbox-column___'];
    }
    Object.keys(data.columns).forEach(c => this.displayedColumns.push(c));

    // Column groups
    this.columnGroups = [];
    if (data.columnGroups)
    {
      Object.keys(data.columnGroups).forEach(c => this.columnGroups.push(c));
    }

    if (this.getSortColumn())
    {
      this.matSort.active = this.getSortColumn()!;
      this.matSort.direction = this.getSortDirection();
    }

    this.activeFilterer = {};
    Object.values(data.data.filtererMap).forEach(filterer =>
    {
      if (filterer.active)
      {
        this.activeFilterer[filterer.column] = filterer;
      }
    });

    this.setHeightScrollableArea();
  }


  private setHeightScrollableArea(): void
  {
    const scrollableArea = this.getScrollableAreaElement();
    if (scrollableArea)
    {
      scrollableArea.style.height = this.heightpx.toString() + 'px';
    }
  }

  private getScrollableAreaElement(): HTMLElement | undefined
  {
    return this.findHtmlElementByName(this.el.nativeElement, 'NG-SCROLLBAR');
  }

  private setHeightScrollbarY(): void
  {
    const scrollableArea = this.getScrollableAreaElement();
    if (scrollableArea)
    {
      const scrollbarY = this.findHtmlElementByName(scrollableArea, 'SCROLLBAR-Y');
      if (scrollbarY)
      {
        const thead = this.findHtmlElementByName(this.el.nativeElement.childNodes[0], 'THEAD');
        const tfoot = this.findHtmlElementByName(this.el.nativeElement.childNodes[0], 'TFOOT');
        const tHeadHeight = thead?.getBoundingClientRect().height ?? 0;
        const tFootHeight = tfoot?.getBoundingClientRect().height ?? 0;
        scrollbarY.style.top = tHeadHeight > 0 ? tHeadHeight.toString() + 'px' : '36px';
        scrollbarY.style.bottom = tFootHeight > 0 ? tFootHeight.toString() + 'px' : '12px';
        scrollbarY.style.display = 'flex';
        //this.scrollable?.update();
      }
    }
  }

  private findHtmlElementByName(rootNode: ChildNode, name: string): HTMLElement | undefined
  {
    return this.findChildNodeByName(rootNode, name) as HTMLElement;
  }

  private findChildNodeByName(rootNode: ChildNode, name: string): ChildNode | undefined
  {
    if (rootNode.nodeName === name)
    {
      return rootNode;
    }

    const childNodes = this.getChildNodes(rootNode);
    for (const node of childNodes)
    {
      const childNode = this.findChildNodeByName(node, name);
      if (childNode !== undefined)
      {
        return childNode;
      }
    }

    return undefined;
  }


  private getChildNodes(node: Node): ChildNode[]
  {
    const children: ChildNode[] = [];
    if (node.hasChildNodes())
    {
      node.childNodes.forEach((child: ChildNode) => children.push(child));
    }

    return children;
  }

  ngAfterViewInit(): void
  {
    this.matTable.dataSource = this.dataSource;

    this.subscriptions.push(merge(this.matSort.sortChange, this.matPaginator.page)
      .pipe(
        tap(() => this.reloadTable())
      )
      .subscribe());

    setTimeout(() => this.setHeightScrollbarY(), 500);
  }

  reloadTable(pageIndex?: number): void
  {
    const paramsChangeEvent: TableViewParamsChangeEvent = {
      paginator: {
        pageIndex: this.matPaginator.pageIndex,
        pageSize: this.matPaginator.pageSize,
        length: this.matPaginator.length,
        pageSizeOptions: this.matPaginator.pageSizeOptions
      },
      sorter: {column: this.matSort.active, direction: this.mapDirection(this.matSort.direction)},
      filtererMap: this.activeFilterer
    };
    this.tableViewParamsChangeEvent.emit(paramsChangeEvent);

    const reloadEvent: TableReloadEvent = {
      page: {index: pageIndex ?? this.matPaginator.pageIndex, size: this.matPaginator.pageSize},
      sort: {column: this.matSort.active, direction: this.mapDirection(this.matSort.direction)},
      filters: this.convertActiveFilterersToFilterList()
    };
    // Fire this one a bit later, so that the viewParamsChange event can submit the changes first
    setTimeout(() => this.tableReloadEvent.emit(reloadEvent), 100);
  }

  convertActiveFilterersToFilterList(): Ngface.DataRetrievalParams.Filter[]
  {
    const c: Ngface.DataRetrievalParams.Filter[] = [];
    Object.values(this.activeFilterer).forEach(filterer =>
    {
      c.push({
        column: filterer.column,
        valueSet: filterer.valueSet.values.filter(v => v.selected).map(v => this.getText(v))
      });
    });
    return c;
  }


  mapDirection(direction: SortDirection): Ngface.Direction
  {
    switch (direction)
    {
      case 'asc':
        return 'ASC';
      case 'desc':
        return 'DESC';
      case '':
        return 'UNDEFINED';
    }
  }

  getText(v: Ngface.ValueSet.Item): DataRetrievalParams.Filter.Item
  {
    return v.text !== '(Blanks)' ? {text: v.text} : {text: null};
  }

  getHeaderText(column: string): string
  {
    const headerText = this.getData().columns[column]?.text;
    return headerText ? headerText : column;
  }

  getCellText(row: Ngface.Row<any>, column: string): string
  {
    const cell = row.cells[column];
    return this.formatCell(cell) ?? 'NULL';
  }

  private formatCell(cell: Ngface.Cell<any, any> | undefined): string | undefined
  {
    if (!cell || cell.value == null)
    {
      return undefined;
    }
    if (cell.type === 'TextCell')
    {
      return cell.value;
    }
    if (cell.type === 'NumericCell')
    {
      const numericCell = cell as NumericCell;
      let formattedNumber = numericCell.format.prefix ?? '' + NumericFormatter.getFormattedValueAsText(numericCell.value, numericCell.format.precision, numericCell.format.digitGrouping, this.locale);
      if (numericCell.format.suffix)
      {
        formattedNumber += ' ' + numericCell.format.suffix;
      }
      return formattedNumber;
    }
    return '';
  }

  getTotalRowCellText(column: string): string
  {
    const cell = this.getData().totalRow?.cells[column];
    return this.formatCell(cell) ?? '';
  }


  getCellLabel(row: Ngface.Row<any>, column: string): string
  {
    const cell = row.cells[column];
    if (!cell.value)
    {
      return '';
    }

    return cell.label;
  }


  getData(): Ngface.Table<any>
  {
    const widget = this.formdata?.widgets[this.widgetid];
    if (!widget || widget?.type !== 'Table')
    {
      return {
        type: 'Table',
        columns: {},
        columnGroups: {},
        rows: [],
        totalRow: null,
        data: {
          type: 'Table.Data',
          paginator: {pageIndex: 0, pageSize: 5, length: 0, pageSizeOptions: []},
          sorter: {column: '', direction: 'UNDEFINED'},
          filtererMap: {}
        },
        label: '',
        enabled: false,
        id: '',
        hint: '',
        selectMode: 'NONE',
        notification: '',
      };
    }
    return this.formdata?.widgets[this.widgetid] as Ngface.Table<any>;
  }

  public isPaginatorEnabled(): boolean
  {
    return !!this.dataSource.paginator;
  }

  getPaginator(): Ngface.Paginator
  {
    if (!this.dataSource.paginator)
    {
      return {pageIndex: 0, pageSize: 5, length: 0, pageSizeOptions: []};
    }

    return this.dataSource.paginator;
  }

  isColumnSortable(column: string): boolean
  {
    const sortable = this.getData().columns[column]?.sortable;
    return sortable !== undefined ? sortable : false;
  }

  getColumnSorter(column: string): Ngface.Sorter | undefined
  {
    const sorter = this.getData().data.sorter;
    return (sorter?.column === column) ? sorter : undefined;
  }

  isColumnFilterable(column: string): boolean
  {
    const filterable = !!this.getData().data.filtererMap[column];
    return filterable ?? false;
  }

  getColumnFilterer(column: string): Ngface.Filterer | undefined
  {
    if (this.activeFilterer[column])
    {
      return this.activeFilterer[column];
    }

    const filterer = this.getData().data.filtererMap[column];
    if (filterer)
    {
      filterer.active = false;
    }
    return filterer;
  }

  getThClass(column: string): string | null
  {
    if (column === '___checkbox-column___')
    {
      return 'size-xsmall';
    }

    switch (this.getData().columns[column]?.size)
    {
      case 'AUTO':
        return 'size-auto';
      case 'XS':
        return 'size-xsmall';
      case 'S':
        return 'size-small';
      case 'M':
        return 'size-medium';
      case 'L':
        return 'size-large';
      case 'XL':
        return 'size-xlarge';
      case 'TIMESTAMP':
        return 'size-timestamp';
      case 'NUMBER':
        return 'size-number';
    }

    return null;
  }

  getTdClass(row: Ngface.Row<any>, column: string): string | null
  {
    const cell = row.cells[column];
    if (cell?.value == null)
    {
      return 'cellvalue-null ' + this.getCellClass(column);
    }

    return this.getCellClass(column);
  }

  private getCellClass(column: string): string | null
  {
    switch (this.getData().columns[column]?.textAlign)
    {
      case 'LEFT':
        return 'align-left';
      case 'CENTER':
        return 'align-center';
      case 'RIGHT':
        return 'align-right';
    }

    return null;
  }

  getTdClassFooter(column: string): string | null
  {
    return this.getCellClass(column);
  }


  isActionCell(row: Ngface.Row<any>, column: string): boolean
  {
    const cell = row.cells[column];
    return cell.type === 'ActionCell';
  }

  getOptionalClasses(): string
  {
    switch (this.getData().selectMode)
    {
      case 'NONE':
        return '';
      case 'SINGLE':
      case 'MULTI':
      case 'CHECKBOX':
        return 'ngface-data-table-selectable';
    }

    return '';
  }

  onRowClick(row: Ngface.Row<any>): void
  {
    if (this.getData().selectMode === 'NONE')
    {
      return
    }

    if (this.getData().selectMode === 'SINGLE')
    {
      this.getData().rows.forEach(r => r.selected = false);
      row.selected = true;
    }
    if (this.getData().selectMode === 'MULTI' || this.getData().selectMode === 'CHECKBOX')
    {
      row.selected = !row.selected;
    }

    this.rowClickEvent.emit(row);
  }

  getRowClasses(row: Ngface.Row<any>): string
  {
    if (row.selected)
    {
      return 'ngface-row-selected';
    }

    return '';
  }

  masterToggle($event: MatCheckboxChange): void
  {
    this.dataSource.getRows().forEach(r => r.selected = $event.checked);
    this.masterToggleEvent.emit({checked: $event.checked});
  }

  isChecked(row: Ngface.Row<any>): boolean
  {
    return row.selected;
  }

  isAnySelected(): boolean
  {
    return !!this.dataSource.getRows().find(r => r.selected);
  }

  isAllSelected(): boolean
  {
    return !this.dataSource.getRows().find(r => !r.selected);
  }

  actionClick(row: Ngface.Row<any>, action: Ngface.Action): void
  {
    this.actionClickEvent.emit({row, actionId: action.id});
  }

  getActions(row: Ngface.Row<any>, column: string): Ngface.Action[] | null
  {
    if (row.cells[column].type === 'ActionCell')
    {
      return (row.cells[column] as ActionCell).value;
    }
    return null;
  }

  getActionClass(action: Ngface.Action): string
  {
    if (action.enabled)
    {
      return 'ngface-mat-icon-enabled';
    }

    return 'ngface-mat-icon-disabled';
  }


  onValueSetSearch(column: string, $event: ValueSetSearchEvent): void
  {
    this.tableValueSetSearchEvent.emit({column, searchEvent: $event});
  }

  onFiltererChange($event: Ngface.Filterer): void
  {
    if ($event)
    {
      this.activeFilterer[$event.column] = $event;
    }
    this.reloadTable(0);
  }


  onFiltererCleared($event: string): void
  {
    delete this.activeFilterer[$event];
    this.reloadTable();
  }


  getNotification(): string
  {
    return this.getData().notification ?? '';
  }

  getSortColumn(): string | null
  {
    const sorter = this.getData().data.sorter;
    return sorter ? sorter.column : null;
  }

  getSortDirection(): SortDirection
  {
    const sorter = this.getData().data.sorter;
    switch (sorter?.direction)
    {
      case 'ASC':
        return 'asc';
      case 'DESC':
        return 'desc';
      case 'UNDEFINED':
      default:
        return '';
    }
  }

  isTotalRow(): boolean
  {
    return !!this.getData().totalRow;
  }

  getColGroupText(columnGroup: string): string
  {
    const columnGroupText = this.getData().columnGroups[columnGroup]?.text;
    return columnGroupText ? columnGroupText : '';
  }

  getColGroupColSpan(columnGroup: string): number
  {
    const colSpan = this.getData().columnGroups[columnGroup]?.colSpan;
    return colSpan ? colSpan : 1;
  }

  getColGroupClass(column: string): string | null
  {
    switch (this.getData().columnGroups[column]?.textAlign)
    {
      case 'LEFT':
        return 'align-left';
      case 'CENTER':
        return 'align-center';
      case 'RIGHT':
        return 'align-right';
    }

    return null;
  }

  hidePageSize(): boolean
  {
    return this.el.nativeElement.offsetWidth < 1000 && this.getData().notification.length > 0;
  }
}
