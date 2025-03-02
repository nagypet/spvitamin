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


  public static createButton(input: Partial<{ id: string, label: string, style: Ngface.Style, enabled: boolean }> = {}): Ngface.Button
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
      enabled: params.enabled
    } as Ngface.Button;
  }
}
