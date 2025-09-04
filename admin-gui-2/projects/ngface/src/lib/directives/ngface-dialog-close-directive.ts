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

// dialog-header-close.directive.ts
import {AfterViewInit, ComponentRef, Directive, ElementRef, OnDestroy, Renderer2, ViewContainerRef} from '@angular/core';
import {DialogCloseBtnComponent} from './close-button.component';

@Directive({
  standalone: true,
  selector: '[ngface-dialog-close]'
})
export class NgfaceDialogCloseDirective implements AfterViewInit, OnDestroy
{
  private compRef!: ComponentRef<DialogCloseBtnComponent>;

  constructor(
    private renderer: Renderer2,
    private host: ElementRef<HTMLElement>,
    private vcr: ViewContainerRef
  )
  {
  }


  ngAfterViewInit(): void
  {
    this.compRef = this.vcr.createComponent(DialogCloseBtnComponent);

    this.renderer.appendChild(
      this.host.nativeElement,
      this.compRef.location.nativeElement
    );
  }


  ngOnDestroy(): void
  {
    this.compRef?.destroy();
  }
}
