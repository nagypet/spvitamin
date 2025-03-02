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

import {Directive, ElementRef, HostListener} from '@angular/core';

@Directive({
    selector: '[numericInputFilter]',
    standalone: true
})
export class NumericInputFilterDirective
{

  constructor(private el: ElementRef)
  {
  }

  @HostListener('keydown', ['$event']) onKeyDown(event: any)
  {
    const e = <KeyboardEvent> event;

    if (['Backspace', 'Tab', 'Enter', 'Delete', 'Escape', '.', ',', '-'].indexOf(e.key) !== -1 ||
      // Allow: Ctrl+A
      (e.key === 'a' && e.ctrlKey === true) ||
      // Allow: Ctrl+C
      (e.key === 'c' && e.ctrlKey === true) ||
      // Allow: Ctrl+X
      (e.key === 'x' && e.ctrlKey === true) ||
      // Allow: Ctrl+V
      (e.key === 'v' && e.ctrlKey === true) ||
      // Allow: home, end, left, right
      (e.keyCode >= 35 && e.keyCode <= 39))
    {
      // let it happen, don't do anything
      return;
    }
    // Ensure that it is a number and stop the keypress
    if (e.shiftKey || e.key < '0' || e.key > '9')
    {
      e.preventDefault();
    }
  }
}
