/*
 * Copyright 2020-2024 the original author or authors.
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

/* tslint:disable:one-line */
import {Injectable} from '@angular/core';
import {ToastrService} from 'ngx-toastr';
import {HttpErrorResponse} from '@angular/common/http';


export interface ErrorObject
{
  httpError: HttpErrorResponse;
  timestamp: Date;
}


@Injectable({
  providedIn: 'root'
})
export class ErrorService
{
  constructor(private toastr: ToastrService)
  {
  }

  errorToast(title: string, message: string): void
  {
    this.toastr.error(message, title);
  }


  warningToast(title: string, message: string): void
  {
    this.toastr.warning(message, title);
  }


  infoToast(title: string, message: string): void
  {
    this.toastr.info(message, title);
  }


  successToast(title: string, message: string): void
  {
    this.toastr.success(message, title);
  }

  handleError(error: HttpErrorResponse): void
  {
    this.toastr.error(error.error, error.statusText);
  }

}
