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

import {Injectable} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgfaceErrorDialogComponent} from '../dialogs/ngface-error-dialog/ngface-error-dialog.component';
import {DeviceTypeService} from './device-type.service';


@Injectable({
    providedIn: 'root'
})
export class ErrorService
{
    private currentError: BehaviorSubject<HttpErrorResponse | undefined> = new BehaviorSubject<HttpErrorResponse | undefined>(undefined);
    private dialogRef?: MatDialogRef<any>;

    constructor(public dialog: MatDialog, private deviceTypeService: DeviceTypeService
    )
    {
    }


    handleError(error: HttpErrorResponse): void
    {
        this.currentError.next(error);
        if (!this.dialogRef)
        {
            this.dialogRef = this.dialog.open(NgfaceErrorDialogComponent, {
                minWidth: this.getErrorDialogWidth(),
                data: this.currentError,
                backdropClass: 'ngface-modal-dialog-backdrop'
            });
        }
    }


    private getErrorDialogWidth(): string
    {
        if (this.deviceTypeService.deviceType === 'Desktop')
        {
            return '800px';
        }

        if (this.deviceTypeService.deviceType === 'Tablet')
        {
            return Math.min(800, this.deviceTypeService.width * 0.8).toString() + 'px';
        }

        return this.deviceTypeService.width.toString() + 'px';
    }


    errorDialogClosing(): void
    {
        this.dialogRef = undefined;
    }
}
