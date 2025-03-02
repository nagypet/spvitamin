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

import {NgZone} from '@angular/core';

export class SseChannel
{
  private eventSource: EventSource | undefined;
  private url: string;
  private reconnectFrequencySec: number = 1;
  private reconnectTimeout: any;
  private lastEventId: string | undefined;
  private onMessage: (event: MessageEvent) => any;
  private onError?: (event: MessageEvent) => any;
  private debugLogEnabled: boolean;
  private zone: NgZone;

  constructor(
    url: string,
    onMessage: (event: MessageEvent) => any,
    zone: NgZone,
    debugLogEnabled: boolean = false,
    onError?: (event: MessageEvent) => any,
  )
  {
    this.url = url;
    this.onMessage = onMessage;
    this.zone = zone;
    this.debugLogEnabled = debugLogEnabled;
    this.onError = onError;
  }


  public open(): boolean
  {
    this.createSseEventSource();
    return !!this.eventSource;
  }

  public close(onError: boolean = false)
  {
    if (this.eventSource)
    {
      this.eventSource.close();
      this.eventSource = undefined;
      if (!onError)
      {
        this.lastEventId = undefined;
        if (this.debugLogEnabled)
        {
          console.log('SSE channel closed and lastEventId discarded!');
        }
      }
      else
      {
        if (this.debugLogEnabled)
        {
          console.log('SSE channel closed!');
        }
      }
    }
  }


  private createSseEventSource(): void
  {
    // Close event source if current instance of SSE service has some
    if (this.eventSource)
    {
      this.close();
    }
    // Open new channel, create new EventSource
    if (this.lastEventId)
    {
      const queryParams = this.url.includes('?') ? '&lastEventId=' + this.lastEventId : '?lastEventId=' + this.lastEventId;
      if (this.debugLogEnabled)
      {
        console.log(this.url + queryParams);
      }
      this.eventSource = new EventSource(this.url + queryParams);
    }
    else
    {
      if (this.debugLogEnabled)
      {
        console.log(this.url);
      }
      this.eventSource = new EventSource(this.url);
    }

    // Process synchronizing event
    this.eventSource.addEventListener('synchronizing', {
      handleEvent: (e: MessageEvent) =>
      {
        if (this.debugLogEnabled)
        {
          console.log('Synchronized with event id: ' + e.lastEventId);
        }
        this.lastEventId = e.lastEventId;
      }
    });

    // Process default event
    this.eventSource.onmessage = (event: MessageEvent) =>
    {
      this.handleServerEvent(event);
    };

    // Process connection opened
    this.eventSource.onopen = () =>
    {
      if (this.debugLogEnabled)
      {
        console.log('SSE channel opened!');
      }
      this.reconnectFrequencySec = 1;
    };

    // Process error
    this.eventSource.onerror = (error: any) =>
    {
      if (this.debugLogEnabled)
      {
        console.log(error);
      }
      this.reconnectOnError();
      this.handleError(error);
    };
  }


  // Handles reconnect attempts when the connection fails for some reason.
  private reconnectOnError(): void
  {
    this.close(true);
    clearTimeout(this.reconnectTimeout);
    if (this.debugLogEnabled)
    {
      console.log('Reconnecting in ' + this.reconnectFrequencySec + ' sec...');
    }
    this.reconnectTimeout = setTimeout(() =>
    {
      this.open();
      this.reconnectFrequencySec *= 2;
      if (this.reconnectFrequencySec >= 10)
      {
        this.reconnectFrequencySec = 10;
      }
    }, this.reconnectFrequencySec * 1000);
  }


  private handleServerEvent(event: MessageEvent): void
  {
    if (this.debugLogEnabled)
    {
      console.log(event);
    }
    this.lastEventId = event.lastEventId;
    this.zone.run(() => this.onMessage(event));
  }

  private handleError(event: MessageEvent): void
  {
    if (this.onError)
    {
      this.zone.run(() => this.onError?.(event));
    }
  }
}
