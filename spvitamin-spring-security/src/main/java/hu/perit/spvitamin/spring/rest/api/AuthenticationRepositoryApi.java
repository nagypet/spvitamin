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
import hu.perit.spvitamin.spring.security.config.AuthenticationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Peter Nagy
 */

@Tag(name = "authentication-repository-controller", description = "Provides the list of configured authentication methods")
public interface AuthenticationRepositoryApi
{
    String BASE_URL = "/api/spvitamin/authentication-repository";

    //------------------------------------------------------------------------------------------------------------------
    // getAuthenticationRepository
    //------------------------------------------------------------------------------------------------------------------
    @GetMapping(BASE_URL)
    @Operation(summary = "getAuthenticationRepository() - Provides the list of configured authentication methods",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = AuthenticationRepository.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = RestExceptionResponse.class)))
            }
    )
    AuthenticationRepository getAuthenticationRepository();
}
