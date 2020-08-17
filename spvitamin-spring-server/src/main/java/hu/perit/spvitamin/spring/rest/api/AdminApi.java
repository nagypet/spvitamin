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

import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameter;
import hu.perit.spvitamin.spring.logging.EventLogId;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Properties;

/**
 * @author Peter Nagy
 */

@Api(value = "admin-api-controller", description = "Admin REST API", tags = "admin-api-controller")
public interface AdminApi
{
    String BASE_URL_ADMIN = "/admin";

    @GetMapping(BASE_URL_ADMIN + "/settings")
    @ApiOperation(value = "Retrieve server settings",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Unauthorized request!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @EventLogId(eventId = 1)
    List<ServerParameter> retrieveServerSettingsUsingGET();

    @GetMapping(BASE_URL_ADMIN + "/version")
    @ApiOperation(value = "Retrieve version info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @EventLogId(eventId = 2)
    Properties retrieveVersionInfoUsingGET();


    @PostMapping(BASE_URL_ADMIN + "/shutdown")
    @ApiOperation(value = "Shuts down the server.",
            authorizations = {@Authorization(value = "Bearer")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Shutdown operation has been started."),
            @ApiResponse(code = 401, message = "Unauthorized request!"),
            @ApiResponse(code = 403, message = "Authenticated user is not allowed to perform the operation!"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @EventLogId(eventId = 3)
    void shutdown();


    @PostMapping(BASE_URL_ADMIN + "/csp_violations")
    @ApiOperation(value = "Logs CSP violations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @EventLogId(eventId = 4)
    void cspViolationsUsingPOST(
            @ApiParam(value = "Violations", required=true ) @RequestBody String request
    );

}
