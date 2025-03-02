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

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({standalone: true, name: 'bytes'})
export class BytesPipe implements PipeTransform
{
  public transform(bytes?: number): string
  {
    if (!bytes)
    {
      return '0';
    }
    if (isNaN(parseFloat('' + bytes)) || !isFinite(bytes))
    {
      return '-';
    }
    if (bytes <= 0)
    {
      return '0';
    }
    const units = ['bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
    const value = Math.floor(Math.log(bytes) / Math.log(1024));
    return (bytes / Math.pow(1024, Math.floor(value))).toFixed(1) + ' ' + units[value];
  }
}
