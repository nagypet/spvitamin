/*
 * Copyright 2020-2022 the original author or authors.
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

import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {AdminService} from '../admin.service';
import {AuthService} from '../auth/auth.service';
import {CertificateFile, KeystoreEntry} from '../../modell/keystore';
import {Router} from '@angular/router';

@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.scss']
})
export class CertificatesComponent implements OnInit {
  @Input('KeystoreType') keystoreType: string;
  @ViewChild('fileInput') fileInput: ElementRef;
  @ViewChild('passwordInput') passwordInput: ElementRef;

  public certFileName: string;
  public certFilePassword: string;

  private certFile: File;
  private certFileOpen: boolean;

  public keystoreEntries: Array<KeystoreEntry> = new Array<KeystoreEntry>();
  public certFileEntries: Array<KeystoreEntry> = new Array<KeystoreEntry>();

  public selectedCertFileEntry: KeystoreEntry = null;

  constructor(
    public adminService: AdminService,
    public authService: AuthService,
    private router: Router,
  ) {
    this.certFile = null;
    this.setCertFileName();
    this.certFileOpen = false;
  }

  ngOnInit() {
    if (this.authService.isAuthenticated()) {
      if (this.keystoreType === 'keystore' || this.router.url === '/admin-gui/keystore') {
        this.keystoreType = 'keystore';
        this.loadKeystoreEntries();
      }
      else {
        this.keystoreType = 'truststore';
        this.loadTruststoreEntries();
      }
    }
  }


  loadKeystoreEntries() {
    this.adminService.getKeystore().subscribe(data => {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (let entry of data) {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
    });
  }


  loadTruststoreEntries() {
    this.adminService.getTruststore().subscribe(data => {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (let entry of data) {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
    });
  }


  onFileSelected(event) {
    console.log(event);

    let fileList: FileList = this.fileInput.nativeElement.files;
    this.certFile = fileList.item(0);
    console.log(this.certFile);

    this.setCertFileName();
  }


  onOpen() {
    if (this.certFile !== null) {
      let fileReader = new FileReader();
      fileReader.onload = (e) => {
        this.getCertFileEntries(<ArrayBuffer>fileReader.result);
      };
      fileReader.readAsArrayBuffer(this.certFile);
    }
  }

  arrayBufferToBase64(buffer) {
    let binary = '';
    let bytes = new Uint8Array(buffer);
    let len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }

  getCertFileEntries(fileReaderResult: ArrayBuffer) {
    let b64 = this.arrayBufferToBase64(fileReaderResult);

    this.certFilePassword = this.passwordInput.nativeElement.value;
    let cert = new CertificateFile(b64, this.certFilePassword);

    this.certFileEntries = new Array<KeystoreEntry>();
    this.adminService.getEntriesFromCert(cert).subscribe(data => {
      for (let entry of data) {
        this.certFileEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
      this.certFileOpen = true;
    });
  }


  importCertificateIntoKeystore(fileReaderResult: ArrayBuffer) {
    let b64 = this.arrayBufferToBase64(fileReaderResult);

    let cert = new CertificateFile(b64, this.certFilePassword);

    this.adminService.importCertificateIntoKeystore(cert, this.selectedCertFileEntry.alias).subscribe(data => {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (let entry of data) {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
      this.certFileOpen = true;
    });
  }


  importCertificateIntoTruststore(fileReaderResult: ArrayBuffer) {
    let b64 = this.arrayBufferToBase64(fileReaderResult);

    let cert = new CertificateFile(b64, this.certFilePassword);

    this.adminService.importCertificateIntoTruststore(cert, this.selectedCertFileEntry.alias).subscribe(data => {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (let entry of data) {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
      this.certFileOpen = true;
    });
  }


  onClose() {
    this.certFile = null;
    this.certFileOpen = false;
    this.certFileEntries = new Array<KeystoreEntry>();
    this.setCertFileName();
  }


  isCertFileOpen(): boolean {
    return this.certFileOpen;
  }


  setCertFileName() {
    if (this.certFile !== null) {
      this.certFileName = this.certFile.name;
    }
    else {
      this.certFileName = 'Choose file';
    }
  }


  onImport() {
    //console.log('Uploading: ' + this.certFileName + ', password: ' + this.certFilePassword + ', alias: ' + this.selectedCertFileEntry.alias)
    if (this.keystoreType === 'keystore') {
      if (this.certFile !== null) {
        let fileReader = new FileReader();
        fileReader.onload = (e) => {
          this.importCertificateIntoKeystore(<ArrayBuffer>fileReader.result);
        };
        fileReader.readAsArrayBuffer(this.certFile);
      }
    }
    else {
      if (this.certFile !== null) {
        let fileReader = new FileReader();
        fileReader.onload = (e) => {
          this.importCertificateIntoTruststore(<ArrayBuffer>fileReader.result);
        };
        fileReader.readAsArrayBuffer(this.certFile);
      }
    }
  }


  onCertSelected(entry: KeystoreEntry) {
    this.selectedCertFileEntry = entry;
  }


  onDelete(alias: string) {
    if (this.keystoreType === 'keystore') {
      this.adminService.removeCertificateFromKeystore(alias).subscribe(data => {
        this.keystoreEntries = new Array<KeystoreEntry>();
        for (let entry of data) {
          this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
        }
      });
    }
    else {
      this.adminService.removeCertificateFromTruststore(alias).subscribe(data => {
        this.keystoreEntries = new Array<KeystoreEntry>();
        for (let entry of data) {
          this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
        }
      });
    }
  }
}
