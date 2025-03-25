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

/* tslint:disable:one-line */
import {Injectable} from '@angular/core';
import { HttpBackend, HttpClient, HttpUrlEncodingCodec } from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {CertificateFile} from '../../model/keystore';
import {SpvitaminAdmin} from '../../model/spvitamin-admin-models';


@Injectable({
  providedIn: 'root'
})
export class AdminService
{
  private httpSilent: HttpClient;

  constructor(
    private http: HttpClient,
    private handler: HttpBackend
  )
  {
    this.httpSilent = new HttpClient(handler);
  }


  private removeWhitespacesFromString(input: string): string
  {
    const codec = new HttpUrlEncodingCodec();
    return codec.encodeValue(input);
    // input.replace(' ', '%20');
  }


  public getVersionInfo(): Observable<any>
  {

    return this.http.get(`${environment.baseURL}/api/spvitamin/admin/version`);
  }

  public getSettings(): Observable<SpvitaminAdmin.ServerSettingsResponse>
  {
    return this.http.get(`${environment.baseURL}/api/spvitamin/admin/settings`) as Observable<SpvitaminAdmin.ServerSettingsResponse>;
  }

  public postShutdown(): Observable<any>
  {
    return this.http.post(`${environment.baseURL}/api/spvitamin/admin/shutdown`, '');
  }

  public getKeystore(): Observable<any>
  {
    return this.http.get(`${environment.baseURL}/api/spvitamin/keystore`);
  }


  public saveKeystore(): Observable<any>
  {
    return this.http.post(`${environment.baseURL}/api/spvitamin/keystore`, null);
  }


  public getEntriesFromCert(certFile: CertificateFile): Observable<any>
  {
    return this.http.post(`${environment.baseURL}/api/spvitamin/keystore/certificates`, certFile);
  }

  public importCertificateIntoKeystore(certFile: CertificateFile, alias: string): Observable<any>
  {
    return this.http.post(`${environment.baseURL}/api/spvitamin/keystore/privatekey`, {certificateFile: certFile, alias: alias});
  }

  public removeCertificateFromKeystore(alias: string): Observable<any>
  {
    // we have to remove white spaces from the alias name
    return this.http.delete(`${environment.baseURL}/api/spvitamin/keystore/privatekey/${this.removeWhitespacesFromString(alias)}`);
  }

  public getTruststore(): Observable<any>
  {
    return this.http.get(`${environment.baseURL}/api/spvitamin/truststore`);
  }

  public importCertificateIntoTruststore(certFile: CertificateFile, alias: string): Observable<any>
  {
    return this.http.post(`${environment.baseURL}/api/spvitamin/truststore/certificate`, {certificateFile: certFile, alias: alias});
  }

  public removeCertificateFromTruststore(alias: string): Observable<any>
  {
    // we have to remove white spaces from the alias name
    return this.http.delete(`${environment.baseURL}/api/spvitamin/truststore/certificate/${this.removeWhitespacesFromString(alias)}`);
  }
}
