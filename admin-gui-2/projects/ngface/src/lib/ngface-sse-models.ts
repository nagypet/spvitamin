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

/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2024-10-03 14:51:41.

export namespace NgfaceSse {

    export interface SseMessageNotification extends SseNotification {
        level: SseMessageNotification.Level;
        message?: string;
        details?: string;
        errorText?: string;
    }

    export interface SseNotification {
        type: SseNotification.Type;
        subject: string;
    }

    export interface SseReloadNotification extends SseNotification {
    }

    export interface SseUpdateNotification<T> extends SseNotification {
        jobIds: T[];
    }

    export namespace SseMessageNotification {

        export type Level = "INFO" | "WARNING" | "ERROR";

    }

    export namespace SseNotification {

        export type Type = "RELOAD" | "MESSAGE" | "UPDATE";

    }

}
