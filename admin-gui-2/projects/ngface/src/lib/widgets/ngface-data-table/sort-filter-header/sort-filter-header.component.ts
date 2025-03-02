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

import {Component, ElementRef, EventEmitter, Input, Output, TemplateRef, ViewChild, ViewContainerRef} from '@angular/core';
import { FilterChangeEvent, ValueSetSearchEvent, ExcelFilterComponent } from '../excel-filter/excel-filter.component';
import {Ngface} from '../../../ngface-models';
import {Overlay, OverlayRef} from '@angular/cdk/overlay';
import {TemplatePortal} from '@angular/cdk/portal';
import { MatIconModule } from '@angular/material/icon';
import { MatSortModule } from '@angular/material/sort';
import { NgIf, NgClass } from '@angular/common';

@Component({
    exportAs: 'ngFaceSortFilterHeader',
    selector: '[ngface-sort-filter-header]',
    templateUrl: './sort-filter-header.component.html',
    styleUrls: ['./sort-filter-header.component.scss'],
    standalone: true,
    imports: [NgIf, MatSortModule, NgClass, MatIconModule, ExcelFilterComponent]
})
export class SortFilterHeaderComponent
{
  @ViewChild('excel_filter') templateExcelFilter!: TemplateRef<unknown>;

  @Input()
  text = '';

  @Input()
  sortable = false;

  @Input()
  sorter?: Ngface.Sorter;

  @Input()
  filterable = false;

  @Input()
  filterer?: Ngface.Filterer;

  @Output()
  searchEvent: EventEmitter<ValueSetSearchEvent> = new EventEmitter();

  @Output()
  filtererChangeEvent: EventEmitter<Ngface.Filterer> = new EventEmitter();

  @Output()
  filtererClearedEvent: EventEmitter<string> = new EventEmitter();

  private overlayRef?: OverlayRef;

  constructor(
    private el: ElementRef,
    private readonly overlay: Overlay,
    private viewContainerRef: ViewContainerRef)
  {
  }

  onFilterIconClick($event: MouseEvent): void
  {
    this.overlayRef = this.overlay.create({
      hasBackdrop: true,
      backdropClass: 'cdk-overlay-transparent-backdrop',
      panelClass: 'mat-elevation-z8',
      scrollStrategy: this.overlay.scrollStrategies.reposition(),
      positionStrategy: this.overlay
        .position()
        .flexibleConnectedTo(this.el)
        .withPositions([
          {
            originX: 'end',
            originY: 'bottom',
            overlayX: 'end',
            overlayY: 'top'
          }
        ])
    });
    const templatePortal = new TemplatePortal(this.templateExcelFilter, this.viewContainerRef);
    this.overlayRef.attach(templatePortal);
    this.overlayRef.backdropClick().subscribe(() => this.overlayRef?.detach());
  }


  onValueSetSearch($event: ValueSetSearchEvent): void
  {
    this.searchEvent.emit($event);
  }

  onFiltererChange($event: FilterChangeEvent): void
  {
    console.log($event);
    this.filterer = $event.filterer;
    if (this.filterer)
    {
      this.filterer.active = $event.changed;
    }
    this.overlayRef?.detach();
    if ($event.changed)
    {
      this.filtererChangeEvent.emit($event.filterer);
    }
    else
    {
      this.filtererClearedEvent.emit($event.filterer?.column);
    }
  }


  isActive(): boolean
  {
    return this.filterer?.active ?? false;
  }

  getClass(): string
  {
    return this.filterer?.active ? 'ngface-filter-header-set' : '';
  }

  onExcelFilterClosed($event: void): void
  {
    this.overlayRef?.detach();
  }
}
