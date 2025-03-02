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

import {HttpHeaders, HttpParams} from '@angular/common/http';

export interface IInput {
  httpUrl: string;
  httpRequestHeaders:
    | HttpHeaders
    | {
        [header: string]: string | string[];
      };
  httpRequestParams:
    | HttpParams
    | {
        [param: string]: string | string[];
      };

  fileAlias: string;
}

export interface IUploadProgress {
  isLoading?: boolean;
  progressPercentage?: number;
  loaded?: number;
  total?: number;
}

export interface IUploadEvent {
  file: File;
  event: any;
}
