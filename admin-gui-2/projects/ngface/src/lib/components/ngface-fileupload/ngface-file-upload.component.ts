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

import {Component, EventEmitter, Input, OnChanges, Output, QueryList, SimpleChanges, ViewChildren} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {MatButton, MatFabButton} from '@angular/material/button';
import {NgForOf, NgIf} from '@angular/common';
import {UploadItemComponent} from './upload-item/upload-item.component';
import {IUploadEvent} from './ngface-file-upload.type';
import {MatBadge} from '@angular/material/badge';

export interface FileStatus
{
  file: File;
  uploaded: boolean;
}

// Adapted this project to my needs: https://github.com/nishantmc/angular-material-fileupload.git

@Component({
  selector: 'ngface-file-upload',
  standalone: true,
  imports: [
    MatIcon,
    MatFabButton,
    MatButton,
    NgIf,
    NgForOf,
    UploadItemComponent,
    MatBadge
  ],
  templateUrl: './ngface-file-upload.component.html',
  styleUrl: './ngface-file-upload.component.css'
})
export class NgfaceFileUploadComponent implements OnChanges
{
  @ViewChildren(UploadItemComponent)
  fileUploads!: QueryList<UploadItemComponent>;

  @Input()
  httpUrl!: string;

  @Input()
  fileType = '*';

  @Input()
  multiple = false;

  @Input()
  autoReset = true;

  @Input()
  reset = false;

  @Input()
  uploadAllColor = 'primary';

  @Input()
  uploadAllLabel = 'Upload All';

  @Input()
  removeAllColor = 'primary';

  @Input()
  removeAllLabel = 'Remove All';

  @Input()
  dropZoneText = 'Select or drag and drop files here';

  @Output() onUpload: EventEmitter<IUploadEvent> = new EventEmitter<IUploadEvent>();

  @Output() onQueued: EventEmitter<File | undefined> = new EventEmitter<File | undefined>();


  public files: Array<FileStatus> = [];


  ngOnChanges(changes: SimpleChanges): void
  {
    if (this.reset)
    {
      this.files = [];
    }
  }


  onFileInput($event: Event): void
  {
    const target = $event.target as HTMLInputElement;

    if (target.files)
    {
      for (let i = 0; i < target.files.length; i++)
      {
        this.files.push({file: target.files[i], uploaded: false});
        console.log(`${i}: ${target.files[i].name}`);
        this.onQueued.emit(target.files[i]);
      }
      console.log(`Added ${this.files.length} files to the queue`);
    }
  }

  uploadAll(): void
  {
    console.log('uploadAll');
    this.fileUploads.forEach((fileUpload) =>
    {
      console.log(fileUpload);
      fileUpload.upload();
    });
  }


  removeAll(): void
  {
    this.onQueued.emit(undefined);
    for (let i = 0; i < this.files.length; i++)
    {
      if (!this.files[i].uploaded)
      {
        this.files.splice(i, 1);
      }
    }
  }


  removeItem($event: UploadItemComponent): void
  {
    this.onQueued.emit(undefined);
    for (let i = 0; i < this.files.length; i++)
    {
      if ($event.file === this.files[i].file)
      {
        this.files.splice(i, 1);
      }
    }
  }


  emitUpload($event: IUploadEvent)
  {
    for (let i = 0; i < this.files.length; i++)
    {
      if ($event.file === this.files[i].file)
      {
        if ($event.event.type === 4)
        {
          if (this.autoReset)
          {
            this.files.splice(i, 1);
          }
          else
          {
            this.files[i].uploaded = true;
          }
        }
      }
    }
    this.onUpload.emit($event);
  }


  public getCountOpen(): number
  {
    let sum = 0;
    for (let i = 0; i < this.files.length; i++)
    {
      if (!this.files[i].uploaded)
      {
        sum++;
      }
    }
    return sum;
  }


  public getBadge(): string
  {
    let countOpen = this.getCountOpen();
    if (countOpen == 0)
    {
      return '';
    }
    return countOpen.toString();
  }
}
