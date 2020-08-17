/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.keystore;

import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.config.JwtProperties;
import hu.perit.spvitamin.spring.config.ServerProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import hu.perit.spvitamin.spring.exception.InvalidInputException;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Peter Nagy
 */

@Log4j
public class KeystoreUtils {

    private KeystoreUtils() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Replace the relative pathes with absolute pathes, before Tomcat is started. Only this solution works in all circumstances.
     */
    public static void locateJksStores() {
        Environment env = SpringEnvironment.getInstance().get();

        if (!Boolean.parseBoolean(env.getProperty("server.ssl.enabled"))) {
            return;
        }

        // keystore
        String keyStoreRelativePath = env.getProperty("server.ssl.key-store");
        if (StringUtils.isBlank(keyStoreRelativePath)) {
            throw new InvalidInputException("server.ssl.key-store must be defined when ssl is enabled!");
        }

        File keyStoreFile = KeystoreUtils.getDesignatedFileLocation(keyStoreRelativePath);
        log.debug(String.format("'%s' => '%s'", keyStoreRelativePath, keyStoreFile.getAbsolutePath()));
        System.setProperty("server.ssl.key-store", keyStoreFile.getAbsolutePath());

        // truststore
        String trustStoreRelativePath = env.getProperty("server.ssl.trust-store");
        File trustStoreFile = KeystoreUtils.getDesignatedFileLocation(trustStoreRelativePath);
        log.debug(String.format("'%s' => '%s'", trustStoreRelativePath, trustStoreFile.getAbsolutePath()));
        System.setProperty("server.ssl.trust-store", trustStoreFile.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStore", trustStoreFile.getAbsolutePath());
    }


    public static KeyStore getServerKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        //server.ssl.key-store-password=tVIVFj+lZIxGqMjc3Q9jkg==
        //server.ssl.key-store=classpath:jks/server-keystore.jks
        ServerProperties serverProperties = SysConfig.getServerProperties();
        String keyStoreName = serverProperties.getSsl().getKeyStore();
        String keyStorePassword = serverProperties.getSsl().getKeyStorePassword();

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        File keyStoreFile = KeystoreUtils.getDesignatedFileLocation(keyStoreName);
        ks.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());

        return ks;
    }


    public static KeyStore getServerTrustStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        ServerProperties serverProperties = SysConfig.getServerProperties();
        String trustStoreName = serverProperties.getSsl().getTrustStore();
        String trustStorePassword = serverProperties.getSsl().getTrustStorePassword();

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        File trustStoreFile = KeystoreUtils.getDesignatedFileLocation(trustStoreName);
        ks.load(new FileInputStream(trustStoreFile), trustStorePassword.toCharArray());

        return ks;
    }


    public static Key getPrivateKey() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        KeyStore serverKeystore = getServerKeyStore();
        JwtProperties jwtProperties = SysConfig.getJwtProperties();
        CryptoUtil crypto = new CryptoUtil();
        return serverKeystore.getKey(jwtProperties.getPrivateKeyAlias(), crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), jwtProperties.getPrivateKeyEncryptedPassword()).toCharArray());
    }


    public static Key getPublicKey() throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {

        KeyStore clientTruststore = getServerTrustStore();
        JwtProperties jwtProperties = SysConfig.getJwtProperties();
        return clientTruststore.getCertificate(jwtProperties.getPublicKeyAlias()).getPublicKey();
    }


    public static List<KeystoreEntry> getSslKeys(KeyStore ks) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException {
        List<KeystoreEntry> retval = new ArrayList<>();

        ServerProperties serverProperties = SysConfig.getServerProperties();

        Enumeration<String> enumeration = ks.aliases();
        while (enumeration.hasMoreElements()) {
            String alias = enumeration.nextElement();

            KeystoreEntry keystoreEntry = new KeystoreEntry();
            keystoreEntry.setAlias(alias);
            String sslKeyAlias = serverProperties.getSsl().getKeyAlias();
            if (sslKeyAlias != null) {
                keystoreEntry.setInUse(sslKeyAlias.equalsIgnoreCase(alias));
            }

            Certificate[] chain = ks.getCertificateChain(alias);

            if (chain != null) {
                for (Certificate certificate : chain) {
                    CertInfo certInfo = getInfoFromCertificate(certificate);
                    keystoreEntry.getChain().add(certInfo);
                    keystoreEntry.setType(KeystoreEntry.EntryType.PRIVATE_KEY_ENTRY);
                }
            }
            else {
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (entry instanceof KeyStore.TrustedCertificateEntry) {
                    KeyStore.TrustedCertificateEntry trustedCertificateEntry = (KeyStore.TrustedCertificateEntry) entry;
                    Certificate certificate = trustedCertificateEntry.getTrustedCertificate();
                    CertInfo certInfo = getInfoFromCertificate(certificate);
                    keystoreEntry.getChain().add(certInfo);
                    keystoreEntry.setType(KeystoreEntry.EntryType.TRUSTED_CERTIFICATE_ENTRY);
                }
            }

            Collections.sort(retval);

            retval.add(keystoreEntry);
        }
        return retval;
    }


    public static CertInfo getInfoFromCertificate(java.security.cert.Certificate certificate) {
        CertInfo certInfo = new CertInfo();
        if (certificate.getType().equalsIgnoreCase("X.509")) {
            X509Certificate x509 = (X509Certificate) certificate;

            certInfo.setValidFrom(x509.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            certInfo.setValidTo(x509.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            certInfo.setIssuer(x509.getIssuerDN().getName());
            certInfo.setSubject(x509.getSubjectDN().getName());
        }
        return certInfo;
    }


    public static File getDesignatedFileLocation(String fileName) {
        fileName = getNameWithoutClasspathPrefix(fileName);

        // Ha már eleve egy abszolut path-t kapunk, és a keystore tényleg ott van...
        File file = new File(fileName);
        if (file.isAbsolute()) {
            log.debug(String.format("'%s' is absolute, no further processing", fileName));
            return file;
        }

        String workingDir = System.getProperty("user.dir");
        File targetFile = new File(workingDir, fileName);

        if (!targetFile.exists()) {
            // try to locate it on the java.class.path, throw exception if not found
            // this is required in debug mode, because when debugging user.dir is set to C:\dev_new\DINA\OnBase\builds\dev\DinaOnBaseServices

            File fileInClassPath = searchInClasspath(fileName);
            if (fileInClassPath != null && fileInClassPath.exists()) {
                targetFile = fileInClassPath;
            }
        }

        if (!targetFile.exists()) {
            throw new InvalidInputException(String.format("'%s' could not be found!", targetFile.toString()));
        }

        return targetFile;
    }


    private static String getNameWithoutClasspathPrefix(String fileName) {
        if (fileName.startsWith("classpath:")) {
            return fileName.substring("classpath:".length());
        }
        else {
            return fileName;
        }
    }


    private static File searchInClasspath(String fileName) {
        String classPath = System.getProperty("java.class.path");
        List<String> pathes = extractClassPathes(classPath);
        return searchInClasspath(pathes, fileName);
    }


    private static File searchInClasspath(List<String> pathes, String fileName) {
        File trustStoreFile = null;
        for (String path : pathes) {
            trustStoreFile = new File(path, fileName);
            if (trustStoreFile.exists()) {
                return trustStoreFile;
            }
        }
        return null;
    }


    private static List<String> extractClassPathes(String classPath) {
        // A classPath vagy egy jar, aminek a manifestjében van a felsorolás, vagy egy pontosvesszővel elválasztott lista
        // C:\Users\nagy_peter\AppData\Local\Temp\classpath1737236763.jar

        Optional<String> optClassPathFromManifest = getClassPathFromManifest(classPath);
        if (optClassPathFromManifest.isPresent()) {
            // file:/C:/Users/nagy_peter/AppData/Local/JetBrains/Toolbox/apps/IDEA-U/ch-0/182.5262.2/lib/idea_rt.jar
            // file:/C:/Users/nagy_peter/AppData/Local/JetBrains/Toolbox/apps/IDEA-U/ch-0/182.5262.2/plugins/junit/lib/junit-rt.jar
            // file:/C:/Users/nagy_peter/AppData/Local/JetBrains/Toolbox/apps/IDEA-U/ch-0/182.5262.2/plugins/junit/lib/junit5-rt.jar
            // file:/C:/dev_new/Innodox/JavaBase/builds/dev/nputil/out/test/classes/
            // file:/C:/dev_new/Innodox/JavaBase/builds/dev/nputil/out/test/resources/
            // file:/C:/dev_new/Innodox/JavaBase/builds/dev/nputil/out/production/classes/
            // file:/C:/dev_new/Innodox/JavaBase/builds/dev/nputil/out/production/resources/
            // file:/C:/Users/nagy_peter/.gradle/caches/modules-2/files-2.1/org.projectlombok/lombok/1.18.8/448003bc1b234aac04b58e27d7755c12c3ec4236/lombok-1.18.8.jar
            // file:/C:/Users/nagy_peter/.gradle/caches/modules-2/files-2.1/commons-io/commons-io/2.6/815893df5f31da2ece4040fe0a12fd44b577afaf/commons-io-2.6.jar
            // file:/C:/Users/nagy_peter/.gradle/caches/modules-2/files-2.1/log4j/log4j/1.2.17/5af35056b4d257e4b64b9e8069c0746e8b08629f/log4j-1.2.17.jar
            // file:/C:/Users/nagy_peter/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar
            // file:/C:/Users/nagy_peter/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar
            return Stream.of(optClassPathFromManifest.get().split("file:/"))
                    .map(String::strip)
                    .filter(entry -> !entry.endsWith(".jar") && !entry.isBlank())
                    .collect(Collectors.toList());
        }
        else {
            return Stream.of(classPath.split(";"))
                    .map(String::strip)
                    .filter(entry -> !entry.endsWith(".jar") && !entry.isBlank())
                    .collect(Collectors.toList());
        }
    }


    private static Optional<String> getClassPathFromManifest(String classPath) {
        // A classPath vagy egy jar, aminek a manifestjében van a felsorolás, vagy egy pontosvesszővel elválasztott lista
        // C:\Users\nagy_peter\AppData\Local\Temp\classpath1737236763.jar

        if (classPath.split(";").length == 1 && classPath.strip().endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(classPath)) {
                return Optional.ofNullable(jarFile.getManifest().getMainAttributes().getValue("Class-Path"));
            }
            catch (IOException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }


    public static void removeEntryFromKeystore(KeyStore ks, String alias) throws KeyStoreException {
        ks.deleteEntry(alias);
    }
}
