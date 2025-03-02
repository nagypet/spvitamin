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
import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Router} from '@angular/router';
import {KeystoreComponent} from './keystore/keystore.component';
import {NgIf} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {CertificateFile, KeystoreEntry} from '../../model/keystore';
import {AdminService} from '../../core/services/admin.service';
import {AuthService} from '../../core/services/auth/auth.service';

@Component({
  selector: 'app-certificates',
  templateUrl: './certificates.component.html',
  styleUrls: ['./certificates.component.scss'],
  imports: [
    KeystoreComponent,
    NgIf,
    MatButtonModule,
    MatFormField,
    MatInput,
    MatLabel
  ],
  standalone: true
})
export class CertificatesComponent implements OnInit
{
  @ViewChild('passwordInput') passwordInput!: ElementRef;

  keystoreType = '';

  public certFileName = '';
  public certFilePassword = '';

  private certFile?: File;
  private certFileOpen: boolean;

  public keystoreEntries: Array<KeystoreEntry> = new Array<KeystoreEntry>();
  public certFileEntries: Array<KeystoreEntry> = new Array<KeystoreEntry>();

  public selectedCertFileEntry!: KeystoreEntry;

  constructor(
    public adminService: AdminService,
    public authService: AuthService,
    private router: Router,
  )
  {
    this.certFile = undefined;
    this.setCertFileName();
    this.certFileOpen = false;
  }

  ngOnInit()
  {
    if (this.router.url === '/admin-gui/keystore')
    {
      this.keystoreType = 'keystore';
      this.loadKeystoreEntries();
    }
    else
    {
      this.keystoreType = 'truststore';
      this.loadTruststoreEntries();
    }
  }


  loadKeystoreEntries()
  {
    this.adminService.getKeystore().subscribe(data =>
    {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (const entry of data)
      {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
    });
  }


  loadTruststoreEntries()
  {
    this.adminService.getTruststore().subscribe(data =>
    {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (const entry of data)
      {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
    });
  }


  onFileSelected($event: any)
  {
    console.log($event);

    const target = $event.target as HTMLInputElement;

    if (target.files && target.files.length > 0)
    {
      this.certFile = target.files[0];
      console.log(this.certFile);
    }

    this.setCertFileName();
  }


  onOpen()
  {
    if (this.certFile)
    {
      const fileReader = new FileReader();
      fileReader.onload = (e) =>
      {
        this.getCertFileEntries(<ArrayBuffer> fileReader.result);
      };
      fileReader.readAsArrayBuffer(this.certFile);
    }
  }

  arrayBufferToBase64(buffer: any)
  {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++)
    {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  }

  getCertFileEntries(fileReaderResult: ArrayBuffer)
  {
    const b64 = this.arrayBufferToBase64(fileReaderResult);

    this.certFilePassword = this.passwordInput.nativeElement.value;
    const cert = new CertificateFile(b64, this.certFilePassword);

    this.certFileEntries = new Array<KeystoreEntry>();
    this.adminService.getEntriesFromCert(cert).subscribe(data =>
    {
      for (const entry of data)
      {
        this.certFileEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
      this.certFileOpen = true;
    });
  }


  importCertificateIntoKeystore(fileReaderResult: ArrayBuffer)
  {
    const b64 = this.arrayBufferToBase64(fileReaderResult);

    const cert = new CertificateFile(b64, this.certFilePassword);

    this.adminService.importCertificateIntoKeystore(cert, this.selectedCertFileEntry.alias).subscribe(data =>
    {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (const entry of data)
      {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
      this.certFileOpen = true;
    });
  }


  importCertificateIntoTruststore(fileReaderResult: ArrayBuffer)
  {
    const b64 = this.arrayBufferToBase64(fileReaderResult);

    const cert = new CertificateFile(b64, this.certFilePassword);

    this.adminService.importCertificateIntoTruststore(cert, this.selectedCertFileEntry.alias).subscribe(data =>
    {
      this.keystoreEntries = new Array<KeystoreEntry>();
      for (const entry of data)
      {
        this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
      }
      this.certFileOpen = true;
    });
  }


  onClose()
  {
    this.certFile = undefined;
    this.certFileOpen = false;
    this.certFileEntries = new Array<KeystoreEntry>();
    this.setCertFileName();
  }


  isCertFileOpen(): boolean
  {
    return this.certFileOpen;
  }


  setCertFileName()
  {
    if (this.certFile)
    {
      this.certFileName = this.certFile.name;
    }
    else
    {
      this.certFileName = 'Choose file';
    }
  }


  onImport()
  {
    // console.log('Uploading: ' + this.certFileName + ', password: ' + this.certFilePassword + ', alias: ' + this.selectedCertFileEntry.alias)
    if (this.keystoreType === 'keystore')
    {
      if (this.certFile)
      {
        const fileReader = new FileReader();
        fileReader.onload = (e) =>
        {
          this.importCertificateIntoKeystore(<ArrayBuffer> fileReader.result);
        };
        fileReader.readAsArrayBuffer(this.certFile);
      }
    }
    else
    {
      if (this.certFile)
      {
        const fileReader = new FileReader();
        fileReader.onload = (e) =>
        {
          this.importCertificateIntoTruststore(<ArrayBuffer> fileReader.result);
        };
        fileReader.readAsArrayBuffer(this.certFile);
      }
    }
  }


  onCertSelected(entry: KeystoreEntry)
  {
    this.selectedCertFileEntry = entry;
  }


  onDelete(alias: string)
  {
    if (this.keystoreType === 'keystore')
    {
      this.adminService.removeCertificateFromKeystore(alias).subscribe(data =>
      {
        this.keystoreEntries = new Array<KeystoreEntry>();
        for (const entry of data)
        {
          this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
        }
      });
    }
    else
    {
      this.adminService.removeCertificateFromTruststore(alias).subscribe(data =>
      {
        this.keystoreEntries = new Array<KeystoreEntry>();
        for (const entry of data)
        {
          this.keystoreEntries.push(new KeystoreEntry(entry.alias, entry.password, entry.inUse, entry.type, entry.valid, entry.chain));
        }
      });
    }
  }
}
