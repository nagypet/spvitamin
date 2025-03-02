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

import {Component, Inject, LOCALE_ID, OnInit} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {BehaviorSubject} from 'rxjs';
import {ErrorService} from '../../services/error.service';
import {A11yModule} from '@angular/cdk/a11y';
import {MatButtonModule} from '@angular/material/button';
import {NgScrollbarModule} from 'ngx-scrollbar';
import {formatDate, NgIf} from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import {ResponsiveClassDirective} from '../../directives/responsive-class-directive';
import {DeviceTypeService} from '../../services/device-type.service';
import {NgfaceButtonComponent} from '../../widgets/ngface-button/ngface-button.component';
import {Ngface} from '../../ngface-models';

export interface SpvitaminErrorResponse
{
  timestamp?: Date;
  status?: number;
  error?: any;
  path: string | null | undefined;
  traceId?: string;
  exception?: ServerExceptionProperties;
  type?: string;
}

export interface ErrorResponse extends SpvitaminErrorResponse
{
  statusText?: string;
  message?: string;
}

export interface ServerExceptionProperties
{
  message: string;
  exceptionClass: string;
  superClasses: string[];
  stackTrace: StackTraceElement[];
  cause: ServerExceptionProperties;
}

export interface StackTraceElement
{
  classLoaderName: string;
  moduleName: string;
  moduleVersion: string;
  methodName: string;
  fileName: string;
  lineNumber: number;
  className: string;
  nativeMethod: boolean;
}

@Component({
  selector: 'lib-ngface-error-dialog',
  templateUrl: './ngface-error-dialog.component.html',
  styleUrls: ['./ngface-error-dialog.component.scss'],
  standalone: true,
  imports: [
    MatDialogModule,
    MatIconModule,
    NgIf,
    NgScrollbarModule,
    MatButtonModule,
    A11yModule,
    ResponsiveClassDirective,
    NgfaceButtonComponent
  ]
})
export class NgfaceErrorDialogComponent implements OnInit
{
  formData = NgfaceErrorDialogComponent.getFormData();
  public showDetails = false;
  private data: ErrorResponse = {
    timestamp: new Date(),
    status: undefined,
    path: undefined,
    traceId: undefined,
    exception: undefined,
    statusText: undefined,
    message: undefined
  };

  constructor(public dialogRef: MatDialogRef<NgfaceErrorDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public inputData: BehaviorSubject<HttpErrorResponse | undefined>,
              @Inject(LOCALE_ID) public locale: string,
              private errorService: ErrorService,
              public deviceTypeService: DeviceTypeService
  )
  {
    inputData.subscribe(error =>
    {
      if (error?.error && error?.error.timestamp && error?.error.exception)
      {
        // This is probably an instance of SpvitaminErrorResponse
        const spvitaminError: SpvitaminErrorResponse = error?.error;
        this.data = {
          timestamp: spvitaminError.timestamp,
          status: spvitaminError.status,
          error: spvitaminError.error,
          path: spvitaminError.path,
          traceId: spvitaminError.traceId,
          exception: spvitaminError.exception,
          type: spvitaminError.type,
          statusText: error.statusText,
          message: spvitaminError.exception?.message
        };
      }
      else
      {
        this.data = {
          timestamp: new Date(),
          status: error?.status,
          path: error?.url,
          statusText: error?.statusText,
          message: error?.message
        };
      }
    });
  }

  private static getFormData(): Ngface.Form
  {
    return {id: 'ngface-error-dialog', title: '', widgets: NgfaceErrorDialogComponent.getWidgets()};
  }


  private static getWidgets(): { [index: string]: Ngface.Button; }
  {
    return {
      'button-close': this.getButton('button-close', 'Close', 'PRIMARY'),
      'button-info': this.getButton('button-info', 'Technical info', 'NONE')
    };
  }

  private static getButton(buttonId: string, label: string, style: Ngface.Style): Ngface.Button
  {
    return {id: buttonId, type: 'Button', label, style, badge: '', hint: '', data: {type: 'VoidWidgetData'}, enabled: true};
  }

  ngOnInit(): void
  {
    this.dialogRef.keydownEvents().subscribe(event =>
    {
      if (event.key === 'Escape')
      {
        this.onCancel();
      }
    });

    this.dialogRef.backdropClick().subscribe(event =>
    {
      this.onCancel();
    });
  }

  onOkClick(): void
  {
    this.errorService.errorDialogClosing();
    this.dialogRef.close();
  }

  private onCancel(): void
  {
    this.errorService.errorDialogClosing();
    this.dialogRef.close();
  }

  onDetailsClick(): void
  {
    this.showDetails = !this.showDetails;
  }

  public getErrorText(): string
  {
    if (this.data?.exception?.exceptionClass === 'hu.perit.spvitamin.core.exception.ApplicationException' ||
      this.data?.exception?.exceptionClass === 'hu.perit.spvitamin.core.exception.ApplicationRuntimeException')
    {
      return this.data?.message ?? 'Unknown error';
    }

    if (this.data.status === 408)
    {
      return this.data?.message ?? 'Timeout';
    }

    return 'An error occurred!';
  }

  public getErrorDetails(): string
  {
    let details = '';
    if (this.data.error)
    {
      details += `${this.data.error}<br><br>`;
    }
    details += `Status: ${this.data.status} - ${this.inputData.value?.statusText}`;
    if (this.data.timestamp)
    {
      const formattedDate = formatDate(this.data.timestamp, 'yyyy-MM-dd hh:mm:ss zzzz', this.locale, '');
      details += `<br>Timestamp: ${formattedDate}`;
    }
    details += `<br>Message: ${this.data.message}`;
    details += `<br>Path: ${this.data.path}`;
    if (this.data.traceId)
    {
      details += `<br>TraceId: ${this.data.traceId}`;
    }
    if (this.data.type)
    {
      details += `<br>Type: ${this.data.type}`;
    }
    details += `<br>`;
    if (this.data.exception)
    {
      details += '<br>' + this.getExceptionAsText(this.data.exception, 0);
    }
    return details;
  }

  private getExceptionAsText(e: ServerExceptionProperties, n: number): string
  {
    const s = `${e.exceptionClass}: ${e.message}`;

    if (!e.cause)
    {
      return s;
    }

    if (n >= 10)
    {
      return s + '<br>...';
    }

    return s + '<br>caused by: ' + this.getExceptionAsText(e.cause, ++n);
  }
}
