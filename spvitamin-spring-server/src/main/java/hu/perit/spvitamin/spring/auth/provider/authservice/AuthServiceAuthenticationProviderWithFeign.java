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

package hu.perit.spvitamin.spring.auth.provider.authservice;

import feign.auth.BasicAuthRequestInterceptor;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.config.SysMicroservices;
import hu.perit.spvitamin.spring.feignclients.SimpleFeignClientBuilder;
import hu.perit.spvitamin.spring.rest.client.TemplateAuthClient;
import hu.perit.spvitamin.spring.rest.model.AuthorizationToken;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

@Service
@Log4j
public class AuthServiceAuthenticationProviderWithFeign extends AuthServiceAuthenticationProvider {

    protected AuthorizationToken getAuthorizationToken(String userName, String password) {
        TemplateAuthClient templateAuthClient = SimpleFeignClientBuilder.newInstance()
                .requestInterceptor(new BasicAuthRequestInterceptor(userName, password))
                .build(TemplateAuthClient.class, getServiceUrl());

        return templateAuthClient.authenticate(null);
    }


    public static String getServiceUrl() {
        SysMicroservices sysMicroservices = SysConfig.getSysMicroservices();
        return sysMicroservices.getAuthService().getUrl();
    }
}
