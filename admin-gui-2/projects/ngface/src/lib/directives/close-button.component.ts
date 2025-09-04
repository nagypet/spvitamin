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
