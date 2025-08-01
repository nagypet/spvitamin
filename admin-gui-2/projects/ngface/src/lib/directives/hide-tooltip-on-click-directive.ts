import {Directive, HostListener} from '@angular/core';
import {MatTooltip} from '@angular/material/tooltip';

@Directive({
  standalone: true,
  selector: '[hideTooltipOnClick]'
})
export class HideTooltipOnClickDirective
{
  constructor(private tooltip: MatTooltip)
  {
  }

  @HostListener('click')
  onClick()
  {
    this.tooltip.hide(0); // Azonnal elrejti a tooltipet
  }
}
