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
  ContentChildren,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList
} from '@angular/core';
import {NgfaceTextInputComponent} from '../../widgets/ngface-text-input/ngface-text-input.component';
import {NgfaceNumericInputComponent} from '../../widgets/ngface-numeric-input/ngface-numeric-input.component';
import {NgfaceDateInputComponent} from '../../widgets/ngface-date-input/ngface-date-input.component';
import {NgfaceDateRangeInputComponent} from '../../widgets/ngface-date-range-input/ngface-date-range-input.component';
import {NgfaceSelectComponent} from '../../widgets/ngface-select/ngface-select.component';
import {FormGroup} from '@angular/forms';
import {Ngface} from '../../ngface-models';
import {NgfaceAutocompleteComponent} from '../../widgets/ngface-autocomplete/ngface-autocomplete.component';
import {NgfaceDateTimeInputComponent} from '../../widgets/ngface-date-time-input/ngface-date-time-input.component';

export class ControlData
{
  data!: { [key: string]: Ngface.WidgetData };
}

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'ngface-form',
  templateUrl: './ngface-form.component.html',
  styleUrls: ['./ngface-form.component.css'],
  standalone: true
})
export class NgfaceFormComponent implements OnInit, OnChanges, AfterViewInit
{

  get formGroup(): FormGroup
  {
    return this.formgroup;
  }

  constructor(private el: ElementRef)
  {
  }

  @Input()
  formdata?: Ngface.Form;

  // Misused here to generate a getter in the web-component
  @Input()
  private formgroup: FormGroup<{}> = new FormGroup({});

  // tslint:disable-next-line:no-output-on-prefix
  @Output()
  onDataChange: EventEmitter<ControlData> = new EventEmitter();

  @ContentChildren(NgfaceTextInputComponent, {descendants: true}) textInputComponents?: QueryList<NgfaceTextInputComponent>;
  @ContentChildren(NgfaceNumericInputComponent, {descendants: true}) numericInputComponents?: QueryList<NgfaceNumericInputComponent>;
  @ContentChildren(NgfaceDateInputComponent, {descendants: true}) dateInputComponents?: QueryList<NgfaceDateInputComponent>;
  @ContentChildren(NgfaceDateTimeInputComponent, {descendants: true}) dateTimeInputComponents?: QueryList<NgfaceDateTimeInputComponent>;
  @ContentChildren(NgfaceDateRangeInputComponent, {descendants: true}) dateRangeInputComponents?: QueryList<NgfaceDateRangeInputComponent>;
  @ContentChildren(NgfaceSelectComponent, {descendants: true}) selectInputComponents?: QueryList<NgfaceSelectComponent>;
  @ContentChildren(NgfaceAutocompleteComponent, {descendants: true}) autocompleteInputComponents?: QueryList<NgfaceAutocompleteComponent>;


  private static getLocalDateTime(date: Date): Date | null
  {
    if (!date)
    {
      return null;
    }

    if (date instanceof Date)
    {
      const offset = date.getTimezoneOffset();
      const convertedDate: Date = new Date();
      convertedDate.setTime(date.getTime() - (offset * 60 * 1000));
      return convertedDate;
    }
    return new Date(date);
  }

  ngOnInit(): void
  {
  }

  ngOnChanges(): void
  {
    this.onDataChange.emit({data: this.getSubmitData()});
  }

  ngAfterViewInit(): void
  {
    // This solution works only in Angular. When using the controls as web-components not.
    this.updateControlsInFormGroup(this.textInputComponents);
    this.updateControlsInFormGroup(this.numericInputComponents);
    this.updateControlsInFormGroup(this.dateInputComponents);
    this.updateControlsInFormGroup(this.dateTimeInputComponents);
    this.updateControlsInFormGroup(this.dateRangeInputComponents);
    this.updateControlsInFormGroup(this.selectInputComponents);
    this.updateControlsInFormGroup(this.autocompleteInputComponents);

    // Listening changes in ContentChildren elements
    this.textInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.textInputComponents));
    this.numericInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.numericInputComponents));
    this.dateInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.dateInputComponents));
    this.dateTimeInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.dateTimeInputComponents));
    this.dateRangeInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.dateRangeInputComponents));
    this.selectInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.selectInputComponents));
    this.autocompleteInputComponents?.changes.subscribe(() => this.updateControlsInFormGroup(this.autocompleteInputComponents));

    // This solution is for the web-component solution
    const allControls = this.getAllNgfaceControls(this.el.nativeElement);
    // @ts-ignore
    allControls.forEach(comp => this.formGroup.addControl(comp.widgetid, comp.get_form_group_item));
  }


  private updateControlsInFormGroup(queryList?: QueryList<any>): void
  {
    queryList?.forEach(comp =>
    {
      if (this.formGroup.contains(comp.widgetid))
      {
        this.formGroup.removeControl(comp.widgetid);
      }
      this.formGroup.addControl(comp.widgetid, comp.formGroupItem);
    });
    //console.log(this.formGroup.controls);
  }


  private getAllNgfaceControls(element: Element): Element[]
  {
    // console.log(element.tagName);
    if (!element)
    {
      return [];
    }

    const retval: Element[] = [];

    // @ts-ignore
    const collection: HTMLCollection = element.children;
    // tslint:disable-next-line:prefer-for-of
    for (let i = 0; i < collection.length; i++)
    {
      const child = collection[i];
      if (this.isNgfaceControl(child))
      {
        retval.push(child);
      }
      const allControlsOfChild = this.getAllNgfaceControls(child);
      allControlsOfChild.forEach((el: Element) => retval.push(el));
    }

    return retval;
  }


  private isNgfaceControl(element: Element): boolean
  {
    return element.localName === 'ngface-date-input-element'
      || element.localName === 'ngface-date-time-input-element'
      || element.localName === 'ngface-date-range-input-element'
      || element.localName === 'ngface-text-input-element'
      || element.localName === 'ngface-numeric-input-element'
      || element.localName === 'ngface-select-element'
      || element.localName === 'ngface-autocomplete-element';
  }


  getSubmitData(): { [key: string]: Ngface.WidgetData }
  {
    const submitData: { [key: string]: Ngface.WidgetData } = {};
    Object.keys(this.formGroup.controls).forEach(controlName =>
    {
      const widget = this.formdata?.widgets[controlName];
      const widgetType: string | undefined = widget?.type;
      switch (widgetType)
      {
        case 'TextInput':
        case 'NumericInput':
        case 'Autocomplete':
          submitData[controlName] = {
            type: widgetType + '.Data',
            value: this.formGroup.controls[controlName]?.value
          } as Ngface.Value<any>;
          break;

        case 'DateInput':
          // Converting to local date without time zone information
          const myDate = this.formGroup.controls[controlName]?.value;
          submitData[controlName] = {
            type: widgetType + '.Data',
            //value: NgfaceFormComponent.getLocalDateTime(myDate)
            value: myDate
          } as Ngface.Value<any>;
          break;

        case 'DateTimeInput':
          const myDateTime = this.formGroup.controls[controlName]?.value;
          submitData[controlName] = {
            type: widgetType + '.Data',
            value: myDateTime
          } as Ngface.Value<any>;
          break;

        case 'DateRangeInput':
          submitData[controlName] = {
            type: widgetType + '.Data',
            // startDate: NgfaceFormComponent.getLocalDateTime(this.formGroup.controls[controlName]?.value?.start),
            // endDate: NgfaceFormComponent.getLocalDateTime(this.formGroup.controls[controlName]?.value?.end)
            startDate: this.formGroup.controls[controlName]?.value?.start,
            endDate: this.formGroup.controls[controlName]?.value?.end
          } as Ngface.DateRangeInput.Data;
          break;

        case 'Select':
          const selected = this.formGroup.controls[controlName]?.value;
          const selectedOption: { [index: string]: string } = {};
          selectedOption[selected] = widget?.data.options[selected];
          submitData[controlName] = {type: widgetType + '.Data', options: selectedOption, selected} as Ngface.Select.Data;
          break;
      }
    });

    return submitData;
  }
}
