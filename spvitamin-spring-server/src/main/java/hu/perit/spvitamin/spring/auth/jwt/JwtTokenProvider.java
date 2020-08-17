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

package hu.perit.spvitamin.spring.auth.jwt;

import hu.perit.spvitamin.spring.config.JwtProperties;
import hu.perit.spvitamin.spring.keystore.KeystoreUtils;
import hu.perit.spvitamin.spring.rest.model.AuthorizationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author Peter Nagy
 */

@Log4j
@AllArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public AuthorizationToken generateToken(String subject, Claims additionalClaims) {
        try {
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiryDate = issuedAt.plusMinutes(jwtProperties.getExpirationInMinutes());
            Key privateKey = KeystoreUtils.getPrivateKey();

            Date iat = Date.from(issuedAt.atZone(ZoneId.systemDefault()).toInstant());
            Date exp = Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant());

            String jwt = Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(iat)
                    .setExpiration(exp)
                    .addClaims(additionalClaims)
                    .signWith(SignatureAlgorithm.RS512, privateKey)
                    .compact();

            return new AuthorizationToken(subject, jwt, issuedAt, expiryDate);
        }
        catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException e) {
            throw new JwtException("Token creation failed!", e);
        }
    }


    public Claims getClaims(String jwt) {
        try {
            Key publicKey = KeystoreUtils.getPublicKey();
            return new DefaultClaims(Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(jwt)
                    .getBody());
        }
        catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException ex) {
            throw new JwtException("JWT token parse failed!", ex);
        }
    }
}
