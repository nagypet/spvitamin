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

package hu.perit.spvitamin.spring.security;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * #know-how:disable-ssl-certificate-validation
 *
 * @author Peter Nagy
 */

abstract class NullTrustManagerFactory extends TrustManagerFactorySpi {

    private NullTrustManager trustManager = null;

    @Override
    protected synchronized void engineInit(KeyStore ks) throws KeyStoreException {
        if (this.trustManager == null) {
            this.trustManager = getInstance();
        }
    }


    @Override
    protected synchronized void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        if (this.trustManager == null) {
            this.trustManager = getInstance();
        }
    }

    abstract NullTrustManager getInstance();


    @Override
    protected synchronized TrustManager[] engineGetTrustManagers() {
        if (this.trustManager == null) {
            this.trustManager = getInstance();
        }
        return new TrustManager[] { this.trustManager };
    }


    public static final class SimpleFactory extends NullTrustManagerFactory {

        @Override
        NullTrustManager getInstance() {
            return new NullTrustManager();
        }
    }
}
