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

package hu.perit.spvitamin.spring.rest.controller;

import com.google.common.reflect.AbstractInvocationHandler;
import hu.perit.spvitamin.core.connectablecontext.StringContextKey;
import hu.perit.spvitamin.core.took.Took;
import hu.perit.spvitamin.spring.config.Constants;
import hu.perit.spvitamin.spring.keystore.KeystoreEntry;
import hu.perit.spvitamin.spring.logging.AbstractInterfaceLogger;
import hu.perit.spvitamin.spring.rest.api.KeystoreApi;
import hu.perit.spvitamin.spring.rest.model.CertificateFile;
import hu.perit.spvitamin.spring.rest.model.ImportCertificateRequest;
import hu.perit.spvitamin.spring.rest.session.KeystoreSession;
import hu.perit.spvitamin.spring.rest.session.KeystoreSessionHolder;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * @author Peter Nagy
 */

@RestController
@Slf4j
public class KeystoreController implements KeystoreApi
{

    private final KeystoreApi proxy;

    // Injecting dependencies
    public KeystoreController(KeystoreSessionHolder userContextHolder, AuthorizationService authorizationService, HttpServletRequest httpRequest) {
        proxy = (KeystoreApi) Proxy.newProxyInstance(
                KeystoreApi.class.getClassLoader(),
                new Class[]{KeystoreApi.class},
                new ProxyImpl(httpRequest, userContextHolder, authorizationService));
    }


    @Override
    public List<KeystoreEntry> retrieveKeystoreEntriesUsingGET() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        return this.proxy.retrieveKeystoreEntriesUsingGET();
    }


    @Override
    public List<KeystoreEntry> readEntriesFromCertificateFileUsingPOST(CertificateFile certFile) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException {
        return this.proxy.readEntriesFromCertificateFileUsingPOST(certFile);
    }


    @Override
    public List<KeystoreEntry> importCertificateIntoKeystoreUsingPOST(ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException {
        return this.proxy.importCertificateIntoKeystoreUsingPOST(request);
    }


    @Override
    public List<KeystoreEntry> removeCertificateFromKeystoreUsingDELETE(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return this.proxy.removeCertificateFromKeystoreUsingDELETE(alias);
    }

    @Override
    public List<KeystoreEntry> retrieveTruststoreEntriesUsingGET() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, IOException, CertificateException {
        return this.proxy.retrieveTruststoreEntriesUsingGET();
    }

    @Override
    public List<KeystoreEntry> importCertificateIntoTruststoreUsingPOST(@Valid ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException {
        return this.proxy.importCertificateIntoTruststoreUsingPOST(request);
    }

    @Override
    public List<KeystoreEntry> removeCertificateFromTruststoreUsingDELETE(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        return this.proxy.removeCertificateFromTruststoreUsingDELETE(alias);
    }


    /*
     * ============== PROXY Implementation =============================================================================
     */

    @Slf4j
    private static class ProxyImpl extends AbstractInvocationHandler {
        private final KeystoreSessionHolder userContextHolder;
        private final AuthorizationService authorizationService;
        private final Logger logger;

        public ProxyImpl(HttpServletRequest httpRequest, KeystoreSessionHolder userContextHolder, AuthorizationService authorizationService) {
            this.userContextHolder = userContextHolder;
            this.authorizationService = authorizationService;
            this.logger = new Logger(httpRequest);
        }


        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            return this.invokeWithExtras(method, args);
        }


        private Object invokeWithExtras(Method method, Object[] args) throws Throwable {
            UserDetails user = this.authorizationService.getAuthenticatedUser();
            try (Took took = new Took(method)) {
                this.logger.traceIn(null, user.getUsername(), method, args);
                KeystoreSession userContext = this.userContextHolder.getContext(new StringContextKey(user.getUsername()));
                Object retval = method.invoke(userContext, args);
                return retval;
            }
            catch (IllegalAccessException ex) {
                this.logger.traceOut(null, user.getUsername(), method, ex);
                throw ex;
            }
            catch (InvocationTargetException ex) {
                this.logger.traceOut(null, user.getUsername(), method, ex.getTargetException());
                throw ex.getTargetException();
            }
        }

        @Slf4j
        private static class Logger extends AbstractInterfaceLogger {

            protected Logger(HttpServletRequest httpRequest) {
                super(httpRequest);
            }

            @Override
            protected String getSubsystemName() {
                return Constants.SUBSYSTEM_NAME;
            }
        }
    }
}
