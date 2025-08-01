// dialog-close-btn.component.ts  –  *standalone*
import { Component, Optional } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule }   from '@angular/material/icon';

@Component({
  standalone: true,
  selector: 'dialog-close-btn',
  imports: [MatButtonModule, MatIconModule],
  template: `
    <button mat-icon-button
            class="close-btn"
            aria-label="Close"
            (click)="dialogRef.close()">
      <mat-icon class="material-symbols-outlined">close</mat-icon>
    </button>
  `,
  styles: [`
    :host { /* a cím elemhez képest */
      position: absolute;
      top: 50%;
      right: 20px;
      transform: translateY(-50%);
      display: flex;
      align-items: center;
    }

    .close-btn {
      width: 30px!important;
      height: 30px!important;
      background: var(--app-primary-palette-600);
      border: solid 1px var(--app-primary-palette-900, blue);
      border-radius: 5px;
      transition: opacity ease 0.2s;
      color: white;
      min-width: 0;
      padding: 0!important;
      line-height: 0;

      &:hover {
        opacity: 0.8;
      }
    }
  `]
})
export class DialogCloseBtnComponent {
  constructor(@Optional() public dialogRef: MatDialogRef<unknown>) {}
}
