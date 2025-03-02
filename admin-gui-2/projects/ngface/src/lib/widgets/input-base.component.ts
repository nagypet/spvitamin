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

import {AbstractControl, FormControl, FormGroupDirective, NgForm, ValidatorFn, Validators} from '@angular/forms';
import {Component, Input, OnChanges, SimpleChange} from '@angular/core';
import {ErrorStateMatcher} from '@angular/material/core';
import {Ngface} from '../ngface-models';

/** Error when invalid control is dirty, touched, or submitted. */
export class MyErrorStateMatcher implements ErrorStateMatcher
{
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean
  {
    const isSubmitted = form && form.submitted;
    return !!(control && control.invalid && (control.dirty || control.touched || isSubmitted));
  }
}


@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-input-base',
  template: ''
})
export abstract class InputBaseComponent implements OnChanges
{
  @Input()
  formdata?: Ngface.Form;

  @Input()
  widgetid = '';

  // tslint:disable-next-line:variable-name
  private _floatLabelControl = new FormControl('auto');
  get floatLabelControl(): FormControl
  {
    return this._floatLabelControl;
  }

  // tslint:disable-next-line:variable-name
  private _formControl = new FormControl('', []);
  get formControl(): FormControl
  {
    return this._formControl;
  }

  get formGroupItem(): AbstractControl
  {
    return this.formControl;
  }


  // Misused here to generate a getter in the web-component
  @Input()
  // tslint:disable-next-line:variable-name
  protected get_form_group_item: AbstractControl = this.formGroupItem;

  errorStateMatcher = new MyErrorStateMatcher();

  protected constructor()
  {
  }


  ngOnChanges(changes: { [propName: string]: SimpleChange }): void
  {
    const value = this.getData().data?.value;

    // Setting the value
    this.formControl.setValue(value);

    // Validators
    const validators = new Array<ValidatorFn>();
    this.getData().validators?.forEach(v =>
    {
      this.createNgValidators(v).forEach(ngValidator => validators.push(ngValidator));
    });
    this.formControl.setValidators(validators);

    // Enabled status
    this.getData().enabled ? this.formControl.enable() : this.formControl.disable();
  }


  protected createNgValidators(validator: Ngface.Validator): ValidatorFn[]
  {
    const validators = new Array<ValidatorFn>();

    switch (validator.type)
    {
      case 'Required':
        validators.push(Validators.required);
        break;

      case 'Min':
        validators.push(Validators.min((validator as Ngface.Min).min));
        break;

      case 'Max':
        validators.push(Validators.max((validator as Ngface.Max).max));
        break;

      case 'Size':
        validators.push(Validators.minLength((validator as Ngface.Size).min));
        validators.push(Validators.maxLength((validator as Ngface.Size).max));
        break;

      case 'Email':
        validators.push(Validators.email);
        break;

      case 'Pattern':
        validators.push(Validators.pattern((validator as Ngface.Pattern).pattern));
        break;

      default:
        console.error('Unknown validator type: ' + validator.type);
    }

    return validators;
  }


  abstract getData(): Ngface.Input<any, any, any>;

  getValue(): string
  {
    return this.getData()?.data?.value;
  }


  /**
   * Returns a validator by name
   * @param name
   * @private
   */
  protected getValidator(name: string): Ngface.Validator | undefined
  {
    let validatorName = name;
    if (name === 'minlength' || name === 'maxlength')
    {
      validatorName = 'Size';
    }
    return this.getData()?.validators?.find(c => c.type.toLowerCase() === validatorName.toLowerCase());
  }


  /**
   * Returns a validator by name
   * @param name
   * @private
   */
  protected getValidatorFrom(validators: Ngface.Validator[] | undefined, name: string): Ngface.Validator | undefined
  {
    let validatorName = name;
    if (name === 'minlength' || name === 'maxlength')
    {
      validatorName = 'Size';
    }
    return validators?.find(c => c.type.toLowerCase() === validatorName.toLowerCase());
  }


  /**
   * Returns true if validators contain "Required"
   */
  isRequired(): boolean
  {
    return !!this.getValidator('Required');
  }


  getMinLength(): number | null
  {
    const sizeValidator = this.getValidator('Size');
    if (sizeValidator)
    {
      return (sizeValidator as Ngface.Size).min;
    }
    return null;
  }


  getMaxLength(): number | null
  {
    const sizeValidator = this.getValidator('Size');
    if (sizeValidator)
    {
      return (sizeValidator as Ngface.Size).max;
    }
    return null;
  }


  getValidationErrors(): string | null
  {
    if (this.formControl.errors)
    {
      return this.getValidationErrorsFromFormControl(this.formControl, this.getData()?.validators).join(' ');
    }

    return null;
  }


  getValidationErrorsFromFormControl(fc: AbstractControl | null, validators: Ngface.Validator[] | undefined): string[]
  {
    const validationErrors = fc?.errors;
    const errorMessages = new Array<string>();
    if (validationErrors)
    {
      Object.keys(validationErrors).forEach(v =>
      {
        const validator = this.getValidatorFrom(validators, v);
        if (validator)
        {
          errorMessages.push(validator.message);
        }
      });
    }
    return errorMessages;
  }
}
