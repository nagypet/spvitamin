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
