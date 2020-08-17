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

package hu.perit.spvitamin.spring.rest.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author Peter Nagy
 */

@Data
@NoArgsConstructor
public class UserDTO {

    // Properties for /users GET
    private Long userId;
    private String userName;
    private String displayName;
    private Set<String> roles;
    private Boolean external;
    private Boolean active;

    // Additional properties for /users{id} GET
    private String address;
    private String email;
    private String phone;
    private Boolean nextLoginChangePwd;

    // Auditing
    private Long createdById;
    private LocalDateTime createdAt;
    private Long updatedById;
    private LocalDateTime updatedAt;
    private Long deletedById;
    private LocalDateTime deletedAt;

    private Boolean deletedFlag;

    private LocalDateTime lastLoginTime;
    private Boolean passwordExpired;
}
