/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2026-03-18 14:36:27.

export namespace NgfaceSse {

    export interface SseMessageNotification extends SseNotification {
        type: "MESSAGE";
        level: SseMessageNotification.Level;
        message?: string;
        details?: string;
        errorText?: string;
    }

    export interface SseNotification extends Serializable {
        type: "MESSAGE" | "RELOAD" | "UPDATE";
        client: string;
        subject: string;
        sender: string;
    }

    export interface SseReloadNotification extends SseNotification {
        type: "RELOAD";
    }

    export interface SseUpdateNotification<T> extends SseNotification {
        type: "UPDATE";
        jobIds: T[];
    }

    export interface Serializable {
    }

    export namespace SseMessageNotification {

        export type Level = "INFO" | "WARNING" | "ERROR";

    }

    export namespace SseNotification {

        export type Type = "RELOAD" | "MESSAGE" | "UPDATE";

    }

}
