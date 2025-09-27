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
import hu.perit.spvitamin.spring.keystore.KeystoreEntry;
import hu.perit.spvitamin.spring.rest.api.KeystoreApi;
import hu.perit.spvitamin.spring.rest.model.CertificateFile;
import hu.perit.spvitamin.spring.rest.model.ImportCertificateRequest;
import hu.perit.spvitamin.spring.rest.session.KeystoreSession;
import hu.perit.spvitamin.spring.rest.session.KeystoreSessionHolder;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
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
    private static final String MODULE_NAME = "keystore-controller";

    private final KeystoreApi proxy;


    // Injecting dependencies
    public KeystoreController(KeystoreSessionHolder userContextHolder, AuthorizationService authorizationService)
    {
        proxy = (KeystoreApi) Proxy.newProxyInstance(
                KeystoreApi.class.getClassLoader(),
                new Class[]{KeystoreApi.class},
                new ProxyImpl(userContextHolder, authorizationService));
    }


    @Override
    @LoggedRestMethod(eventId = 10, module = MODULE_NAME)
    public List<KeystoreEntry> retrieveKeystoreEntriesUsingGET() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException
    {
        return this.proxy.retrieveKeystoreEntriesUsingGET();
    }


    @Override
    @LoggedRestMethod(eventId = 11, module = MODULE_NAME)
    public List<KeystoreEntry> readEntriesFromCertificateFileUsingPOST(CertificateFile certFile) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException
    {
        return this.proxy.readEntriesFromCertificateFileUsingPOST(certFile);
    }


    @Override
    @LoggedRestMethod(eventId = 12, module = MODULE_NAME)
    public List<KeystoreEntry> importCertificateIntoKeystoreUsingPOST(ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException
    {
        return this.proxy.importCertificateIntoKeystoreUsingPOST(request);
    }


    @Override
    @LoggedRestMethod(eventId = 13, module = MODULE_NAME)
    public List<KeystoreEntry> removeCertificateFromKeystoreUsingDELETE(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
        return this.proxy.removeCertificateFromKeystoreUsingDELETE(alias);
    }


    @Override
    @LoggedRestMethod(eventId = 14, module = MODULE_NAME)
    public List<KeystoreEntry> retrieveTruststoreEntriesUsingGET() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, IOException, CertificateException
    {
        return this.proxy.retrieveTruststoreEntriesUsingGET();
    }


    @Override
    @LoggedRestMethod(eventId = 15, module = MODULE_NAME)
    public List<KeystoreEntry> importCertificateIntoTruststoreUsingPOST(@Valid ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException
    {
        return this.proxy.importCertificateIntoTruststoreUsingPOST(request);
    }


    @Override
    @LoggedRestMethod(eventId = 16, module = MODULE_NAME)
    public List<KeystoreEntry> removeCertificateFromTruststoreUsingDELETE(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
        return this.proxy.removeCertificateFromTruststoreUsingDELETE(alias);
    }


    /*
     * ============== PROXY Implementation =============================================================================
     */


    @Slf4j
    private static class ProxyImpl extends AbstractInvocationHandler
    {
        private final KeystoreSessionHolder userContextHolder;
        private final AuthorizationService authorizationService;


        public ProxyImpl(KeystoreSessionHolder userContextHolder, AuthorizationService authorizationService)
        {
            this.userContextHolder = userContextHolder;
            this.authorizationService = authorizationService;
        }


        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable
        {
            return this.invokeWithExtras(method, args);
        }


        private Object invokeWithExtras(Method method, Object[] args) throws Throwable
        {
            UserDetails user = this.authorizationService.getAuthenticatedUser();
            try (Took took = new Took(method))
            {
                KeystoreSession userContext = this.userContextHolder.getContext(new StringContextKey(user.getUsername()));
                return method.invoke(userContext, args);
            }
            catch (IllegalAccessException ex)
            {
                throw ex;
            }
            catch (InvocationTargetException ex)
            {
                throw ex.getTargetException();
            }
        }
    }
}
