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

import {Ngface} from '../ngface-models';

export class FileDownloadUtils
{
  public static getFilenameFromContentDisposition(contentDisposition: string | null | undefined): string
  {
    let filename = 'downloaded-file';

    if (contentDisposition)
    {
      // First try to get filename* parameter (UTF-8 encoded)
      const filenameStarMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
      if (filenameStarMatch && filenameStarMatch[1])
      {
        filename = decodeURIComponent(filenameStarMatch[1]);
      }
      else
      {
        // Fallback to regular filename parameter
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match && match[1])
        {
          filename = match[1];
        }
      }
    }
    return filename;
  }
}
