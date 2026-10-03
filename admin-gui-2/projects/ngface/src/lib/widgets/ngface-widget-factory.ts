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

import {Ngface} from '../ngface-models';

export class NgfaceWidgetFactory
{

  public static createTextInput(input: Partial<{
    id: string;
    label: string;
    value: string;
    validators: Ngface.Validator[];
    password: boolean
  }> = {}): Ngface.TextInput
  {
    const defaults = {
      id: '',
      label: 'undefined label',
      value: '',
      validators: [],
      password: false
    };

    const params = {...defaults, ...input};

    return {
      id: params.id,
      type: 'TextInput',
      password: params.password,
      label: params.label,
      placeholder: params.label,
      hint: '',
      data: {type: 'TextInput.Data', value: params.value},
      enabled: true,
      validators: params.validators
    } as Ngface.TextInput;
  }


  public static createRemoteAutocomplete(input: Partial<{
    id: string;
    label: string;
    placeholder: string;
    value: string;
    validators: Ngface.Validator[];
    valueSet: Ngface.ValueSet;
  }> = {}): Ngface.Autocomplete
  {
    const defaults = {
      id: '',
      label: 'undefined label',
      placeholder: '',
      value: '',
      validators: [],
      valueSet: {remote: true, truncated: false, values: []}
    };

    const params = {...defaults, ...input};

    return {
      id: params.id,
      type: 'Autocomplete',
      label: params.label,
      placeholder: params.placeholder,
      hint: '',
      data: {type: 'Autocomplete.Data', value: params.value, extendedReadOnlyData: {valueSet: params.valueSet}},
      enabled: true,
      validators: params.validators
    } as Ngface.Autocomplete;
  }


  public static createRemoteGenericAutocomplete(input: Partial<{
    id: string;
    label: string;
    placeholder: string;
    value: Ngface.AbstractOption;
    validators: Ngface.Validator[];
    valueSet: Ngface.GenericValueSet<Ngface.AbstractOption>;
  }> = {}): Ngface.GenericAutocomplete<Ngface.AbstractOption>
  {
    const defaults = {
      id: '',
      label: 'undefined label',
      placeholder: '',
      //value: {id: '', type: '', texts: ['alma']},
      value: null,
      validators: [],
      valueSet: {remote: true, truncated: false, values: []}
    };

    const params = {...defaults, ...input};

    return {
      id: params.id,
      type: 'GenericAutocomplete',
      label: params.label,
      placeholder: params.placeholder,
      hint: '',
      data: {type: 'GenericAutocomplete.Data', value: params.value, extendedReadOnlyData: {valueSet: params.valueSet}},
      enabled: true,
      validators: params.validators
    } as Ngface.GenericAutocomplete<Ngface.AbstractOption>;
  }


  public static createButton(input: Partial<{ id: string, label: string, style: Ngface.Button.Style, enabled: boolean }> = {}): Ngface.Button
  {
    const defaults = {
      id: '',
      label: 'undefined label',
      style: 'PRIMARY',
      enabled: true
    };

    const params = {...defaults, ...input};

    return {
      id: params.id,
      type: 'Button',
      label: params.label,
      style: params.style,
      enabled: params.enabled,
      options: null
    } as Ngface.Button;
  }


  public static createSelect(input: Partial<{
    id: string;
    label: string;
    options: { [index: string]: string };
    selected: string;
    validators: Ngface.Validator[];
  }> = {}): Ngface.Select
  {
    const defaults = {
      id: '',
      label: 'undefined label',
      validators: []
    };

    const params = {...defaults, ...input};

    return {
      id: params.id,
      type: 'Select',
      label: params.label,
      placeholder: params.label,
      hint: '',
      data: {type: 'Select.Data', options: params.options, selected: params.selected},
      enabled: true,
      validators: params.validators
    } as Ngface.Select;
  }
}
