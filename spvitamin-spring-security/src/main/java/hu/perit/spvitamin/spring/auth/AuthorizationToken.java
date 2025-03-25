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

package hu.perit.spvitamin.spring.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This class is only a wrapper around the token, basically for use in the frontend. The content is more or less
 * identical with the payload of the token.
 * The backend uses always the signed token to restore the authentication information.
 *
 * @author Peter Nagy
 */

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
public class AuthorizationToken implements AbstractAuthorizationToken
{

    private String sub;
    @JsonProperty("preferred_username")
    private String preferredUsername;
    private String jwt;
    private LocalDateTime iat;
    private LocalDateTime exp;
    private Long uid;
    private Set<String> rls;
    private String source;
}
