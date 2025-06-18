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

import {Injectable} from '@angular/core';
import {fromEvent, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class IdleService
{
  public refreshToken$: Subject<void> = new Subject();
  private refreshDisabled = false;

  constructor()
  {
    // Setup events
    fromEvent(document, 'click').subscribe(() => this.onInteraction());
    fromEvent(document, 'touchstart').subscribe(() => this.onInteraction());
    fromEvent(document, 'keydown').subscribe(() => this.onInteraction());
  }

  onInteraction(): void
  {
    if (!this.refreshDisabled)
    {
      this.refreshToken$.next();
      this.refreshDisabled = true;

      setTimeout(() =>
      {
        this.refreshDisabled = false;
      }, 30_000);
    }
  }
}
