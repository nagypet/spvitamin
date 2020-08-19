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

import hu.perit.spvitamin.spring.rest.model.AuthorizationToken;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Peter Nagy
 */

@Api(value = "auth-api-controller", description = "Authentication and authorization", tags = "auth-api-controller")
public interface AuthApi {

    String BASE_URL_AUTHENTICATE = "/authenticate";

    /*
     * ============== authenticate ===================================================================================
     */
    @GetMapping(BASE_URL_AUTHENTICATE)
    @ApiOperation(value = "authenticate() - User authenticate",
            authorizations = {@Authorization(value = "basicAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 401, message = "Invalid credentials"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @ResponseStatus(value = HttpStatus.OK)
    AuthorizationToken authenticateUsingGET(
            @ApiParam(value = "ProcessID", required = false) @RequestHeader(value = "processID", required = false) String processID
    );
}
