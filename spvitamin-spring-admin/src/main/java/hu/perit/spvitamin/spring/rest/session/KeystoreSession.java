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

package hu.perit.spvitamin.spring.rest.session;

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.core.connectablecontext.ActivityLock;
import hu.perit.spvitamin.core.connectablecontext.ConnectableContext;
import hu.perit.spvitamin.spring.config.ServerProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.keystore.CertInfo;
import hu.perit.spvitamin.spring.keystore.KeystoreEntry;
import hu.perit.spvitamin.spring.keystore.KeystoreUtils;
import hu.perit.spvitamin.spring.rest.api.KeystoreApi;
import hu.perit.spvitamin.spring.rest.model.CertificateFile;
import hu.perit.spvitamin.spring.rest.model.ImportCertificateRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.List;

/**
 * @author Peter Nagy
 */

@Slf4j
public class KeystoreSession extends ConnectableContext implements KeystoreApi {

    private static final int IDLETIMESEC = 30;

    private KeyStore serverKeystore;
    private KeyStore serverTruststore;

    @Override
    protected long getIdleTimeout() {
        return IDLETIMESEC;
    }


    KeystoreSession() {
        this.serverKeystore = KeystoreUtils.getServerKeyStore();
        this.serverTruststore = KeystoreUtils.getServerTrustStore();
    }


    @Override
    public List<KeystoreEntry> retrieveKeystoreEntriesUsingGET() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        try (ActivityLock lock = new ActivityLock(this)) {
            return KeystoreUtils.getSslKeys(this.serverKeystore);
        }
    }


    @Override
    public List<KeystoreEntry> readEntriesFromCertificateFileUsingPOST(CertificateFile certFile) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException {
        try (ActivityLock lock = new ActivityLock(this)) {
            byte[] decodedBytes = Base64.getDecoder().decode(certFile.getContent());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            // try if input is a keystore
            try {
                ks.load(new ByteArrayInputStream(decodedBytes), certFile.getPassword().toCharArray());
            }
            catch (IOException ex) {
                // this must be a simple cert
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(new ByteArrayInputStream(decodedBytes));

                // import cert
                ks.load(null, null);
                CertInfo certInfo = KeystoreUtils.getInfoFromCertificate(cert);
                ks.setCertificateEntry(certInfo.getSubjectCN(), cert);
            }

            return KeystoreUtils.getSslKeys(ks);
        }
    }


    @Override
    public List<KeystoreEntry> importCertificateIntoKeystoreUsingPOST(ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException {
        try (ActivityLock lock = new ActivityLock(this)) {

            // get cert keystore
            byte[] decodedBytes = Base64.getDecoder().decode(request.getCertificateFile().getContent());

            KeyStore certKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            certKeystore.load(new ByteArrayInputStream(decodedBytes), request.getCertificateFile().getPassword().toCharArray());

            // import cert
            Key key = certKeystore.getKey(request.getAlias(), request.getCertificateFile().getPassword().toCharArray());
            if (key != null) {
                Certificate[] chain = certKeystore.getCertificateChain(request.getAlias());
                CertInfo certInfo = KeystoreUtils.getInfoFromCertificate(chain[0]);
                ServerProperties serverProperties = SysConfig.getServerProperties();
                String keyStorePassword = serverProperties.getSsl().getKeyStorePassword();

                this.serverKeystore.setKeyEntry(certInfo.getSubjectCN(), key, keyStorePassword.toCharArray(), chain);
            }
            else {
                throw new RuntimeException(String.format("'%s' is not a private key, cannot be imported into the keystore!", request.getAlias()));
            }

            // save
            this.saveKeystore();

            // return new server keystore
            return KeystoreUtils.getSslKeys(this.serverKeystore);
        }
    }


    private void saveKeystore() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        if (this.serverKeystore == null) {
            throw new RuntimeException("There is no keystore configured in this server!");
        }

        ServerProperties serverProperties = SysConfig.getServerProperties();
        String keyStoreName = serverProperties.getSsl().getKeyStore();
        String keyStorePassword = serverProperties.getSsl().getKeyStorePassword();

        File keystoreFile = KeystoreUtils.getDesignatedFileLocation(keyStoreName);

        try (FileOutputStream outputStream = new FileOutputStream(keystoreFile)) {
            this.serverKeystore.store(outputStream, keyStorePassword.toCharArray());
            log.info(String.format("Kestore successfully saved to: '%s'", keystoreFile.toString()));
        }
        catch (CertificateException
                | NoSuchAlgorithmException
                | KeyStoreException
                | IOException e) {
            log.error(StackTracer.toString(e));
            throw e;
        }
    }


    @Override
    public List<KeystoreEntry> removeCertificateFromKeystoreUsingDELETE(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        try (ActivityLock lock = new ActivityLock(this)) {

            KeystoreUtils.removeEntryFromKeystore(this.serverKeystore, alias);

            // save
            this.saveKeystore();

            return KeystoreUtils.getSslKeys(this.serverKeystore);
        }
    }


    @Override
    public List<KeystoreEntry> retrieveTruststoreEntriesUsingGET() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        try (ActivityLock lock = new ActivityLock(this)) {

            if (this.serverTruststore == null) {
                throw new RuntimeException("There is no truststore configured in this server!");
            }

            return KeystoreUtils.getSslKeys(this.serverTruststore);
        }
    }


    @Override
    public List<KeystoreEntry> importCertificateIntoTruststoreUsingPOST(ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException {
        try (ActivityLock lock = new ActivityLock(this)) {

            if (this.serverTruststore == null) {
                throw new RuntimeException("There is no truststore configured in this server!");
            }

            // try if input is a keystore
            byte[] decodedBytes = Base64.getDecoder().decode(request.getCertificateFile().getContent());
            try {
                KeyStore certKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
                certKeystore.load(new ByteArrayInputStream(decodedBytes), request.getCertificateFile().getPassword().toCharArray());

                // import cert
                this.serverTruststore.setCertificateEntry(request.getAlias(), certKeystore.getCertificate(request.getAlias()));
            }
            catch (IOException ex) {
                // this must be a simple cert
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(new ByteArrayInputStream(decodedBytes));

                // import cert
                this.serverTruststore.setCertificateEntry(request.getAlias(), cert);
            }

            // save
            this.saveTruststore();

            // return new server keystore
            return KeystoreUtils.getSslKeys(this.serverTruststore);
        }
    }


    private void saveTruststore() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        if (this.serverTruststore == null) {
            throw new RuntimeException("There is no truststore configured in this server!");
        }

        ServerProperties serverProperties = SysConfig.getServerProperties();
        String trustStoreName = serverProperties.getSsl().getTrustStore();
        String trustStorePassword = serverProperties.getSsl().getTrustStorePassword();

        File trustStoreFile = KeystoreUtils.getDesignatedFileLocation(trustStoreName);

        try (FileOutputStream outputStream = new FileOutputStream(trustStoreFile)) {
            this.serverTruststore.store(outputStream, trustStorePassword.toCharArray());
            log.info(String.format("Truststore successfully saved to: '%s'", trustStoreFile.toString()));
        }
        catch (CertificateException
                | NoSuchAlgorithmException
                | KeyStoreException
                | IOException e) {
            log.error(StackTracer.toString(e));
            throw e;
        }
    }


    @Override
    public List<KeystoreEntry> removeCertificateFromTruststoreUsingDELETE(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        try (ActivityLock lock = new ActivityLock(this)) {

            if (this.serverTruststore == null) {
                throw new RuntimeException("There is no truststore configured in this server!");
            }

            KeystoreUtils.removeEntryFromKeystore(this.serverTruststore, alias);

            // save
            this.saveTruststore();

            return KeystoreUtils.getSslKeys(this.serverTruststore);
        }
    }


    private File getTempDir() {
        String workingDir = System.getProperty("user.dir");

        return new File(Paths.get(workingDir, "temp", this.getContextName()).toString());
    }


    @Override
    public void disconnect() {
        super.disconnect();

        try {
            // delete the temp dir
            File tempDir = this.getTempDir();
            if (tempDir.exists()) {
                log.debug(String.format("DEL '%s'", tempDir.getAbsolutePath()));
                FileUtils.deleteDirectory(tempDir);
            }
        }
        catch (IOException e) {
            log.error(StackTracer.toString(e));
        }
    }
}
