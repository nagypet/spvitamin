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

package hu.perit.spvitamin.spring.keystore;

import hu.perit.spvitamin.core.crypto.CryptoException;
import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.config.JwtProperties;
import hu.perit.spvitamin.spring.config.JwtPropertiesPublic;
import hu.perit.spvitamin.spring.config.ServerProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.environment.SpringEnvironment;
import hu.perit.spvitamin.spring.exception.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Peter Nagy
 */

@Slf4j
public class KeystoreUtils
{

    private static final String SERVER_SSL_KEYSTORE = "server.ssl.key-store";
    private static final String SERVER_SSL_TRUSTSTORE = "server.ssl.trust-store";


    private KeystoreUtils()
    {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Replace the relative pathes with absolute pathes, before Tomcat is started. Only this solution works in all circumstances.
     */
    public static void locateJksStores()
    {
        Environment env = SpringEnvironment.get();

        if (!Boolean.parseBoolean(env.getProperty("server.ssl.enabled")))
        {
            return;
        }

        // keystore
        String keyStoreRelativePath = env.getProperty(SERVER_SSL_KEYSTORE);
        if (StringUtils.isBlank(keyStoreRelativePath))
        {
            throw new InvalidInputException(SERVER_SSL_KEYSTORE + " must be defined when ssl is enabled!");
        }

        File keyStoreFile = KeystoreUtils.getDesignatedFileLocation(keyStoreRelativePath);
        log.debug(String.format("'%s' => '%s'", keyStoreRelativePath, keyStoreFile.getAbsolutePath()));
        System.setProperty(SERVER_SSL_KEYSTORE, keyStoreFile.getAbsolutePath());

        // truststore
        String trustStoreRelativePath = env.getProperty(SERVER_SSL_TRUSTSTORE);
        if (StringUtils.isNotBlank(trustStoreRelativePath))
        {
            File trustStoreFile = KeystoreUtils.getDesignatedFileLocation(trustStoreRelativePath);
            log.debug(String.format("'%s' => '%s'", trustStoreRelativePath, trustStoreFile.getAbsolutePath()));
            System.setProperty(SERVER_SSL_TRUSTSTORE, trustStoreFile.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStore", trustStoreFile.getAbsolutePath());
        }
    }


    public static KeyStore getServerKeyStore()
    {
        try
        {
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
        catch (Exception ex)
        {
            throw new CryptoException(ex);
        }
    }


    public static KeyStore getServerTrustStore()
    {
        try
        {

            ServerProperties serverProperties = SysConfig.getServerProperties();
            String trustStoreName = serverProperties.getSsl().getTrustStore();
            String trustStorePassword = serverProperties.getSsl().getTrustStorePassword();

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

            File trustStoreFile = KeystoreUtils.getDesignatedFileLocation(trustStoreName);
            ks.load(new FileInputStream(trustStoreFile), trustStorePassword.toCharArray());

            return ks;
        }
        catch (Exception ex)
        {
            throw new CryptoException(ex);
        }
    }


    public static Key getPrivateKey()
    {
        JwtProperties jwtProperties = SysConfig.getJwtProperties();
        Key key = null;
        try
        {
            KeyStore serverKeystore = getServerKeyStore();
            CryptoUtil crypto = new CryptoUtil();
            key = serverKeystore.getKey(jwtProperties.getPrivateKeyAlias(),
                    crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), jwtProperties.getPrivateKeyEncryptedPassword()).toCharArray());
        }
        catch (Exception ex)
        {
            throw new CryptoException(ex);
        }

        if (key == null)
        {
            throw new InvalidInputException(
                    String.format("'%s' not found in keystore '%s'!", jwtProperties.getPrivateKeyAlias(), System.getProperty(SERVER_SSL_KEYSTORE)));
        }
        return key;
    }


    public static PublicKey getPublicKey()
    {

        JwtPropertiesPublic jwtProperties = SysConfig.getJwtPropertiesPublic();
        Certificate certificate = null;
        try
        {
            KeyStore clientTruststore = getServerTrustStore();
            certificate = clientTruststore.getCertificate(jwtProperties.getPublicKeyAlias());
        }
        catch (Exception ex)
        {
            throw new CryptoException(ex);
        }

        if (certificate == null)
        {
            throw new InvalidInputException(
                    String.format("'%s' not found in truststore '%s'!", jwtProperties.getPublicKeyAlias(), System.getProperty(SERVER_SSL_TRUSTSTORE)));
        }
        return certificate.getPublicKey();
    }


    public static List<KeystoreEntry> getSslKeys(KeyStore ks) throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException
    {
        List<KeystoreEntry> retval = new ArrayList<>();

        ServerProperties serverProperties = SysConfig.getServerProperties();

        Enumeration<String> enumeration = ks.aliases();
        while (enumeration.hasMoreElements())
        {
            String alias = enumeration.nextElement();

            KeystoreEntry keystoreEntry = new KeystoreEntry();
            keystoreEntry.setAlias(alias);
            String sslKeyAlias = serverProperties.getSsl().getKeyAlias();
            if (sslKeyAlias != null)
            {
                keystoreEntry.setInUse(sslKeyAlias.equalsIgnoreCase(alias));
            }

            Certificate[] chain = ks.getCertificateChain(alias);

            if (chain != null)
            {
                for (Certificate certificate : chain)
                {
                    CertInfo certInfo = getInfoFromCertificate(certificate);
                    keystoreEntry.getChain().add(certInfo);
                    keystoreEntry.setType(KeystoreEntry.EntryType.PRIVATE_KEY_ENTRY);
                }
            }
            else
            {
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (entry instanceof KeyStore.TrustedCertificateEntry trustedCertificateEntry)
                {
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


    public static CertInfo getInfoFromCertificate(java.security.cert.Certificate certificate)
    {
        CertInfo certInfo = new CertInfo();
        if (certificate.getType().equalsIgnoreCase("X.509"))
        {
            X509Certificate x509 = (X509Certificate) certificate;

            certInfo.setValidFrom(x509.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            certInfo.setValidTo(x509.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            certInfo.setIssuer(x509.getIssuerDN().getName());
            certInfo.setSubject(x509.getSubjectDN().getName());
        }
        return certInfo;
    }


    public static File getDesignatedFileLocation(String fileName)
    {
        fileName = getNameWithoutClasspathPrefix(fileName);

        // Ha már eleve egy abszolut path-t kapunk, és a keystore tényleg ott van...
        File file = new File(fileName);
        if (file.isAbsolute())
        {
            log.debug(String.format("'%s' is absolute, no further processing", fileName));
            return file;
        }

        String workingDir = System.getProperty("user.dir");
        File targetFile = new File(workingDir, fileName);

        if (!targetFile.exists())
        {
            // try to locate it on the java.class.path, throw exception if not found
            // this is required in debug mode, because when debugging user.dir is set to C:\dev_new\DINA\OnBase\builds\dev\DinaOnBaseServices

            File fileInClassPath = searchInClasspath(fileName);
            if (fileInClassPath != null && fileInClassPath.exists())
            {
                targetFile = fileInClassPath;
            }
        }

        if (!targetFile.exists())
        {
            throw new InvalidInputException(String.format("'%s' could not be found!", targetFile.toString()));
        }

        return targetFile;
    }


    private static String getNameWithoutClasspathPrefix(String fileName)
    {
        if (fileName.startsWith("classpath:"))
        {
            return fileName.substring("classpath:".length());
        }
        else
        {
            return fileName;
        }
    }


    private static File searchInClasspath(String fileName)
    {
        String classPath = System.getProperty("java.class.path");
        List<String> pathes = extractClassPathes(classPath);
        return searchInClasspath(pathes, fileName);
    }


    private static File searchInClasspath(List<String> pathes, String fileName)
    {
        //log.debug(String.format("searchInClasspath() fileName: '%s', classpath: '%s'", fileName, pathes.toString()));
        File trustStoreFile = null;
        for (String path : pathes)
        {
            trustStoreFile = new File(path, fileName);
            if (trustStoreFile.exists())
            {
                return trustStoreFile;
            }
        }
        return null;
    }


    private static List<String> extractClassPathes(String classPath)
    {
        //log.debug(String.format("extractClassPathes() classpath: '%s'", classPath));
        // A classPath vagy
        // -- egy jar, aminek a manifestjében van a felsorolás (C:\Users\nagy_peter\AppData\Local\Temp\classpath1737236763.jar),
        // -- vagy egy pontosvesszővel elválasztott lista (Windows)
        // -- vagy egy kettősponttal elválasztott lista (Linux)

        Optional<String> optClassPathFromManifest = getClassPathFromManifest(classPath);
        if (optClassPathFromManifest.isPresent())
        {
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
        else
        {
            return Stream.of(classPath.split(File.pathSeparator))
                    .map(String::strip)
                    .filter(entry -> !entry.endsWith(".jar") && !entry.isBlank())
                    .collect(Collectors.toList());
        }
    }


    private static Optional<String> getClassPathFromManifest(String classPath)
    {
        // A classPath vagy egy jar, aminek a manifestjében van a felsorolás, vagy egy pontosvesszővel elválasztott lista
        // C:\Users\nagy_peter\AppData\Local\Temp\classpath1737236763.jar

        if (classPath.split(";").length == 1 && classPath.strip().endsWith(".jar"))
        {
            try (JarFile jarFile = new JarFile(classPath))
            {
                return Optional.ofNullable(jarFile.getManifest().getMainAttributes().getValue("Class-Path"));
            }
            catch (IOException e)
            {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }


    public static void removeEntryFromKeystore(KeyStore ks, String alias) throws KeyStoreException
    {
        ks.deleteEntry(alias);
    }
}
