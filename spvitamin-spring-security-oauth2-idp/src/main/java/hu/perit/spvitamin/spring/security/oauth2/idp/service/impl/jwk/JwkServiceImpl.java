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

package hu.perit.spvitamin.spring.security.oauth2.idp.service.impl.jwk;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import hu.perit.spvitamin.spring.keystore.KeystoreUtils;
import hu.perit.spvitamin.spring.security.oauth2.idp.service.api.JwkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwkServiceImpl implements JwkService
{
    private static final String KID = "26add321-2331-4c20-bdf7-6a418345a6ae";


    @Override
    public Map<String, Object> getJwks() throws Exception
    {
        JWK jwk = buildJwkFromKeystore();
        JWKSet jwkSet = new JWKSet(jwk);
        return jwkSet.toJSONObject();
    }


    private JWK buildJwkFromKeystore()
    {
        PublicKey publicKey = KeystoreUtils.getPublicKey();
        if (publicKey instanceof RSAPublicKey rsa)
        {
            return new RSAKey.Builder(rsa)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(KID)
                    //.x509CertChain(Collections.singletonList(Base64.encode(cert.getEncoded())))
                    .build();
        }
        // EC/EdDSA támogatás is megoldható itt, ha ilyen a kulcs
        throw new IllegalStateException("Unsupported key type: " + publicKey.getAlgorithm());
    }
}
