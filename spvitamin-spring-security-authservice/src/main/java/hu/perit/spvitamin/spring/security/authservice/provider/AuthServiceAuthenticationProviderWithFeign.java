/*
 * Copyright 2020-2024 the original author or authors.
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

package hu.perit.spvitamin.spring.security.authservice.provider;

import org.springframework.stereotype.Service;

import feign.auth.BasicAuthRequestInterceptor;
import hu.perit.spvitamin.spring.auth.AuthorizationToken;
import hu.perit.spvitamin.spring.config.MicroserviceCollectionProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.feignclients.SimpleFeignClientBuilder;
import hu.perit.spvitamin.spring.security.authservice.restclient.AuthClient;

@Service
public class AuthServiceAuthenticationProviderWithFeign extends AuthServiceAuthenticationProvider {

    protected AuthorizationToken getAuthorizationToken(String userName, String password) {
        AuthClient templateAuthClient = SimpleFeignClientBuilder.newInstance()
                .requestInterceptor(new BasicAuthRequestInterceptor(userName, password))
                .build(AuthClient.class, getServiceUrl());

        return templateAuthClient.authenticate(null);
    }


    public static String getServiceUrl() {
        MicroserviceCollectionProperties microserviceCollectionProperties = SysConfig.getSysMicroservices();
        return microserviceCollectionProperties.get("auth-service").getUrl();
    }
}
