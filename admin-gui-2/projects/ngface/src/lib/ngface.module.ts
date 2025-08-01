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

import {NgModule} from '@angular/core';
import {NgfaceButtonComponent} from './widgets/ngface-button/ngface-button.component';
import {CommonModule} from '@angular/common';
import {MatTooltipModule} from '@angular/material/tooltip';
import {NgfaceDataTableComponent} from './widgets/ngface-data-table/ngface-data-table.component';
import {NgfaceDateInputComponent} from './widgets/ngface-date-input/ngface-date-input.component';
import {NgfaceDateRangeInputComponent} from './widgets/ngface-date-range-input/ngface-date-range-input.component';
import {NgfaceNumericInputComponent} from './widgets/ngface-numeric-input/ngface-numeric-input.component';
import {NgfaceSelectComponent} from './widgets/ngface-select/ngface-select.component';
import {NgfaceTextInputComponent} from './widgets/ngface-text-input/ngface-text-input.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {IntlNumericInputComponent} from './widgets/ngface-numeric-input/intl-numeric-input/intl-numeric-input.component';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatTableModule} from '@angular/material/table';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {SortFilterHeaderComponent} from './widgets/ngface-data-table/sort-filter-header/sort-filter-header.component';
import {MatIconModule} from '@angular/material/icon';
import {ExcelFilterComponent} from './widgets/ngface-data-table/excel-filter/excel-filter.component';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatNativeDateModule} from '@angular/material/core';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from '@angular/platform-browser';
import {MatSortModule} from '@angular/material/sort';
import {MatButtonModule} from '@angular/material/button';
import {NumericInputFilterDirective} from './directives/numeric-input-filter-directive';
import {DebounceInputDirective} from './directives/debounce-input-directive';
import {NgScrollbarModule} from 'ngx-scrollbar';
import {MatBadgeModule} from '@angular/material/badge';
import {SafeHtmlPipe} from './directives/safe-html.pipe';
import {NgfaceFormComponent} from './form/ngface-form/ngface-form.component';
import {HTTP_INTERCEPTORS} from '@angular/common/http';
import {ErrorInterceptor} from './interceptors/error-interceptor.service';
import {NgfaceErrorDialogComponent} from './dialogs/ngface-error-dialog/ngface-error-dialog.component';
import {OverlayModule} from '@angular/cdk/overlay';
import {A11yModule} from '@angular/cdk/a11y';
import {MatDialogModule} from '@angular/material/dialog';
import {NgfaceAutocompleteComponent} from './widgets/ngface-autocomplete/ngface-autocomplete.component';
import {NgfaceTitlebarComponent} from './titlebar/ngface-titlebar/ngface-titlebar.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {ResponsiveClassDirective} from './directives/responsive-class-directive';
import {NgfaceFileUploadComponent} from './components/ngface-fileupload/ngface-file-upload.component';
import {NgfaceDialogCloseDirective} from './directives/ngface-dialog-close-directive';


@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CommonModule,
    MatTooltipModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatBadgeModule,
    FormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatCheckboxModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    NgScrollbarModule,
    OverlayModule,
    A11yModule,
    MatDialogModule,
    NgfaceButtonComponent,
    NgfaceDataTableComponent,
    NgfaceDateInputComponent,
    NgfaceDateRangeInputComponent,
    NgfaceNumericInputComponent,
    NgfaceSelectComponent,
    NgfaceTextInputComponent,
    IntlNumericInputComponent,
    SortFilterHeaderComponent,
    ExcelFilterComponent,
    NumericInputFilterDirective,
    DebounceInputDirective,
    SafeHtmlPipe,
    NgfaceFormComponent,
    NgfaceErrorDialogComponent,
    NgfaceAutocompleteComponent,
    MatToolbarModule,
    NgfaceTitlebarComponent,
    ResponsiveClassDirective,
    NgfaceFileUploadComponent,
    NgfaceDialogCloseDirective
  ],
  exports: [
    NgfaceButtonComponent,
    NgfaceDataTableComponent,
    NgfaceDateInputComponent,
    NgfaceDateRangeInputComponent,
    NgfaceNumericInputComponent,
    NgfaceSelectComponent,
    NgfaceTextInputComponent,
    NgfaceFormComponent,
    NgfaceAutocompleteComponent,
    SafeHtmlPipe,
    ResponsiveClassDirective,
    NgfaceFileUploadComponent,
    NgfaceDialogCloseDirective
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorInterceptor,
      multi: true
    }
  ]
})
export class NgfaceModule
{
}
