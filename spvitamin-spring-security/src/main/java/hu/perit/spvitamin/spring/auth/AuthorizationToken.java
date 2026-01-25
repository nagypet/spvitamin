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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import hu.perit.spvitamin.json.time.CustomInstantDeserializer;
import hu.perit.spvitamin.spring.security.CredentialType;
import hu.perit.spvitamin.spring.security.auth.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
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
    private JwtTokenProvider.Type type;
    private String sub;
    @JsonProperty("preferred_username")
    private String preferredUsername;
    private String jwt;
    @JsonDeserialize(using = CustomInstantDeserializer.class)
    private Instant iat;
    @JsonDeserialize(using = CustomInstantDeserializer.class)
    private Instant exp;
    private String uid;
    private String clientId;
    private Set<String> rls;
    private Set<String> scope;
    private String source;
    private String sid;
    private Map<String, Serializable> additionalClaims;
    private Map<String, Serializable> ext;
    @JsonProperty("credential_type")
    private CredentialType credentialType;


    public AuthorizationToken clone()
    {
        return AuthorizationToken.builder()
                .type(this.type)
                .sub(this.sub)
                .preferredUsername(this.preferredUsername)
                .jwt(this.jwt)
                .iat(this.iat)
                .exp(this.exp)
                .uid(this.uid)
                .clientId(this.clientId)
                .rls(this.rls)
                .scope(this.scope)
                .source(this.source)
                .sid(this.sid)
                .additionalClaims(this.additionalClaims)
                .credentialType(this.credentialType)
                .build();
    }
}
