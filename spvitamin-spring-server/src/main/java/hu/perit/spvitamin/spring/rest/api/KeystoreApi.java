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

package hu.perit.spvitamin.spring.rest.api;

import hu.perit.spvitamin.spring.keystore.KeystoreEntry;
import hu.perit.spvitamin.spring.logging.EventLogId;
import hu.perit.spvitamin.spring.rest.model.CertificateFile;
import hu.perit.spvitamin.spring.rest.model.ImportCertificateRequest;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * @author Peter Nagy
 */

@Api(value = "keystore-api-controller", description = "Keystore REST API", tags = "keystore-api-controller")
public interface KeystoreApi
{
    String BASE_URL_KEYSTORE = "/keystore";
    String BASE_URL_TRUSTSTORE = "/truststore";

    @GetMapping(BASE_URL_KEYSTORE)
    @ApiOperation(value = "Retrieve keystore content",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 10)
    List<KeystoreEntry> retrieveKeystoreEntriesUsingGET() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException;


    @PostMapping(BASE_URL_KEYSTORE + "/certificates")
    @ApiOperation(value = "Retrieve content of a certificate file",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 11)
    List<KeystoreEntry> readEntriesFromCertificateFileUsingPOST(
            @ApiParam(value = "Certificate file", required=true ) @Valid @RequestBody CertificateFile certFile) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException;


    @PostMapping(BASE_URL_KEYSTORE + "/privatekey")
    @ApiOperation(value = "Import a private key into the server keystore",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 12)
    List<KeystoreEntry> importCertificateIntoKeystoreUsingPOST(
            @ApiParam(value = "Import certificate request", required=true ) @Valid @RequestBody ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException;


    @DeleteMapping(BASE_URL_KEYSTORE + "/privatekey/{alias}")
    @ApiOperation(value = "Remove a private key from the server keystore",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 13)
    List<KeystoreEntry> removeCertificateFromKeystoreUsingDELETE(
            @ApiParam(value = "Alias",required=true) @PathVariable("alias") String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException;


    @GetMapping(BASE_URL_TRUSTSTORE)
    @ApiOperation(value = "Retrieve truststore content",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 14)
    List<KeystoreEntry> retrieveTruststoreEntriesUsingGET() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException;


    @PostMapping(BASE_URL_TRUSTSTORE + "/certificate")
    @ApiOperation(value = "Import a certificate into the server truststore",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 15)
    List<KeystoreEntry> importCertificateIntoTruststoreUsingPOST(
            @ApiParam(value = "Import certificate request", required=true ) @Valid @RequestBody ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException;


    @DeleteMapping(BASE_URL_TRUSTSTORE + "/certificate/{alias}")
    @ApiOperation(value = "Remove a certificate from the server truststore",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "User is not authenticated!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error"),
    })
    @EventLogId(eventId = 16)
    List<KeystoreEntry> removeCertificateFromTruststoreUsingDELETE(
            @ApiParam(value = "Alias",required=true) @PathVariable("alias") String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException;
}
