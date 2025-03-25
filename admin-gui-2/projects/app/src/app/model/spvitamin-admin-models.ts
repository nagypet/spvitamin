/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-03-25 06:56:53.

export namespace SpvitaminAdmin {

    export interface CertificateFile {
        content: string;
        password: string;
    }

    export interface ImportCertificateRequest {
        certificateFile: CertificateFile;
        alias: string;
    }

    export interface ServerSettingsResponse {
        serverParameters: { [index: string]: ServerParameter[] };
    }

    export interface ServerParameter extends Comparable<ServerParameter> {
        name: string;
        value: string;
        link: boolean;
    }

    export interface Comparable<T> {
    }

}
