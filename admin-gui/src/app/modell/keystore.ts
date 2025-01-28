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

export class CertInfo
{
  issuerCN: string;
  subjectCN: string;
  validFrom: Date;
  validTo: Date;
  valid: boolean;
}


export class KeystoreEntry
{
  constructor(
    public alias: string,
    public password: string,
    public inUse: boolean,
    public type: string,
    public valid: boolean,
    public chain: CertInfo[])
  {}

  getTypeAbbr(): string
  {
    switch (this.type)
    {
      case "PRIVATE_KEY_ENTRY":
        return "PK";

      default:
        return "";
    }
  }
}


export class CertificateFile
{
  constructor(
    public content: string,
    public password: string
  ) {}
}
