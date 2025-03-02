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
// Generated using typescript-generator version 3.2.1263 on 2024-08-19 13:46:31.

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
