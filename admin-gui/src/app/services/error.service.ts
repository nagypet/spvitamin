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
