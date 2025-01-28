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

package hu.perit.spvitamin.spring.rest.api;

import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import hu.perit.spvitamin.spring.keystore.KeystoreEntry;
import hu.perit.spvitamin.spring.logging.EventLogId;
import hu.perit.spvitamin.spring.rest.model.CertificateFile;
import hu.perit.spvitamin.spring.rest.model.ImportCertificateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * @author Peter Nagy
 */

@Tag(name = "keystore-api-controller", description = "Keystore REST API")
public interface KeystoreApi
{
    String BASE_URL_KEYSTORE = "/api/spvitamin/keystore";
    String BASE_URL_TRUSTSTORE = "/api/spvitamin/truststore";

    @GetMapping(BASE_URL_KEYSTORE)
    @Operation(summary = "Retrieve keystore content",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 10)
    List<KeystoreEntry> retrieveKeystoreEntriesUsingGET() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException;


    @PostMapping(BASE_URL_KEYSTORE + "/certificates")
    @Operation(summary = "Retrieve content of a certificate file",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 11)
    List<KeystoreEntry> readEntriesFromCertificateFileUsingPOST(
            @Parameter(name = "Certificate file", required = true) @Valid @RequestBody CertificateFile certFile) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException;


    @PostMapping(BASE_URL_KEYSTORE + "/privatekey")
    @Operation(summary = "Import a private key into the server keystore",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 12)
    List<KeystoreEntry> importCertificateIntoKeystoreUsingPOST(
            @Parameter(name = "Import certificate request", required = true) @Valid @RequestBody ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException;


    @DeleteMapping(BASE_URL_KEYSTORE + "/privatekey/{alias}")
    @Operation(summary = "Remove a private key from the server keystore",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 13)
    List<KeystoreEntry> removeCertificateFromKeystoreUsingDELETE(
            @Parameter(name = "Alias", required = true) @PathVariable("alias") String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException;


    @GetMapping(BASE_URL_TRUSTSTORE)
    @Operation(summary = "Retrieve truststore content",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 14)
    List<KeystoreEntry> retrieveTruststoreEntriesUsingGET() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException;


    @PostMapping(BASE_URL_TRUSTSTORE + "/certificate")
    @Operation(summary = "Import a certificate into the server truststore",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 15)
    List<KeystoreEntry> importCertificateIntoTruststoreUsingPOST(
            @Parameter(name = "Import certificate request", required = true) @Valid @RequestBody ImportCertificateRequest request) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException;


    @DeleteMapping(BASE_URL_TRUSTSTORE + "/certificate/{alias}")
    @Operation(summary = "Remove a certificate from the server truststore",
            security = {@SecurityRequirement(name = "bearer")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to perform the operation!", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    @EventLogId(eventId = 16)
    List<KeystoreEntry> removeCertificateFromTruststoreUsingDELETE(
            @Parameter(name = "Alias", required = true) @PathVariable("alias") String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException;
}
