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

import {Directive, HostListener, Input, Output} from "@angular/core";
import {Subject, timer} from "rxjs";
import {debounce, distinctUntilChanged} from "rxjs/operators";

@Directive({
    selector: '[debounceInput]',
    standalone: true
})
export class DebounceInputDirective
{
  @Input()
  debounceTime: number = 0;

  @HostListener('input', ['$event'])
  onInput(event: UIEvent): void
  {
    this.value$.next((event.target as HTMLInputElement).value);
  }

  private value$ = new Subject<string>();

  @Output()
  readonly debounceInput = this.value$.pipe(
    debounce(() => timer(this.debounceTime || 0)),
    distinctUntilChanged()
  );
}
