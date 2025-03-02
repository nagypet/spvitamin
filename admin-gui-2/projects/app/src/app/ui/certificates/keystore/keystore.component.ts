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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgForOf, NgIf} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../../dialogs/confirmation-dialog/confirmation-dialog.component';
import {CertInfo, KeystoreEntry} from '../../../model/keystore';
import {AuthService} from '../../../core/services/auth/auth.service';


@Component({
  selector: 'app-keystore',
  templateUrl: './keystore.component.html',
  styleUrls: ['./keystore.component.scss'],
  imports: [
    NgForOf,
    NgIf
  ],
  standalone: true
})
export class KeystoreComponent implements OnInit, OnChanges
{
  @Input('Keystore') keystore!: Array<KeystoreEntry>;
  @Input('DeleteAllowed') deleteAllowed = true;
  @Output('Selected') certSelected = new EventEmitter<KeystoreEntry>();
  @Output('OnDelete') onDeleteEmitter = new EventEmitter<string>();

  selected?: KeystoreEntry;

  constructor(
    public authService: AuthService,
    public dialog: MatDialog
  )
  {
  }

  ngOnInit()
  {
  }

  onSelectEntry(entry: KeystoreEntry)
  {
    this.selected = entry;
    this.certSelected.emit(entry);
  }

  isEntrySelected(entry: KeystoreEntry): boolean
  {
    if (this.selected == null)
    {
      return false;
    }
    return (entry.alias === this.selected.alias);
  }

  onSelectCert(entry: CertInfo)
  {
  }

  isCertSelected(entry: CertInfo): boolean
  {
    return false;
  }

  ngOnChanges(changes: SimpleChanges): void
  {
    if (this.keystore.length === 0)
    {
      this.selected = undefined;
    }
  }

  getClass(entry: KeystoreEntry)
  {
    let classes = 'list-group-item list-group-item-action d-flex justify-content-between';

    if (entry.inUse === true)
    {
      classes += ' list-group-item-primary';
    }
    else if (entry.valid !== true)
    {
      classes += ' list-group-item-warning';
    }
    else
    {
      classes += ' list-group-item-light';
    }

    return classes;
  }

  onDelete(entry: KeystoreEntry)
  {
    this.dialog.open(ConfirmationDialogComponent, {
      minWidth: 500,
      data: entry.alias,
      backdropClass: 'ngface-modal-dialog-backdrop'
    }).afterClosed().subscribe((result: string) =>
    {
      if (result === 'OK')
      {
        this.onDeleteEmitter.emit(entry.alias);
      }
    });
  }

}
