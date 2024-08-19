/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2024-08-19 08:11:08.

export namespace Spvitamin {

    export interface CertificateFile {
        content: string;
        password: string;
    }

    export interface ImportCertificateRequest {
        certificateFile: CertificateFile;
        alias: string;
    }

    export interface ServerSettingsResponse {
        serverParameters: ServerParameter[];
    }

    export interface ServerParameter extends Comparable<ServerParameter> {
        name: string;
        value: string;
        link: boolean;
    }

    export interface Comparable<T> {
    }

}
