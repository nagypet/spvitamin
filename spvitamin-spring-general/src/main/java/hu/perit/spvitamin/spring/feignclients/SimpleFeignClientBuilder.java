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

package hu.perit.spvitamin.spring.feignclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import hu.perit.spvitamin.spring.config.SpringContext;

/**
 * @author Peter Nagy
 */

public class SimpleFeignClientBuilder {

    private Feign.Builder builder;

    public static SimpleFeignClientBuilder newInstance() {
        return new SimpleFeignClientBuilder();
    }


    public SimpleFeignClientBuilder() {
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        this.builder = Feign.builder()
                .decoder(new JacksonDecoder(objectMapper))
                .encoder(new JacksonEncoder(objectMapper))
                .retryer(Retryer.NEVER_RETRY)
                .errorDecoder(new RestExceptionResponseDecoder());
    }


    public SimpleFeignClientBuilder requestInterceptor(RequestInterceptor requestInterceptor) {
        this.builder.requestInterceptor(requestInterceptor);
        return this;
    }


    public <T> T build(Class<T> apiType, String url) {
        return this.builder.target(apiType, url);
    }
}
