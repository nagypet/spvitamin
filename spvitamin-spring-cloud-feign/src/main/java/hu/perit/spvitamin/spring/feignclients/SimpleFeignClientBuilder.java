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

package hu.perit.spvitamin.spring.feignclients;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import hu.perit.spvitamin.spring.config.FeignProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SysConfig;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

/**
 * @author Peter Nagy
 */

public class SimpleFeignClientBuilder
{
    private final Feign.Builder builder;
    private final RequestInterceptorAdapter requestInterceptorAdapter = new RequestInterceptorAdapter();

    public static SimpleFeignClientBuilder newInstance()
    {
        return new SimpleFeignClientBuilder();
    }


    public SimpleFeignClientBuilder()
    {
        // Adding the TracingFeignInterceptor
        this.requestInterceptorAdapter.addInterceptor(new TracingFeignInterceptor());

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        FeignProperties feignProperties = SysConfig.getFeignProperties();
        this.builder = Feign.builder()
                .contract(new SpringMvcContract())
                .requestInterceptor(this.requestInterceptorAdapter)
                .decoder(new JacksonDecoder(objectMapper))
                .encoder(new JacksonEncoder(objectMapper))
                .retryer(new Retryer.Default(feignProperties.getRetry().getPeriod(), feignProperties.getRetry().getMaxPeriod(), feignProperties.getRetry().getMaxAttempts()))
                //.retryer(Retryer.NEVER_RETRY)
                .logger(new Slf4jLogger(getClass()))
                .logLevel(getLevel(feignProperties.getLoggerLevel()))
                .errorDecoder(new RestExceptionResponseDecoder());
    }


    private static Logger.Level getLevel(String level)
    {
        return Logger.Level.valueOf(level.toUpperCase());
    }


    public SimpleFeignClientBuilder requestInterceptor(RequestInterceptor requestInterceptor)
    {
        this.requestInterceptorAdapter.addInterceptor(requestInterceptor);
        return this;
    }


    public <T> T build(Class<T> apiType, String url)
    {
        return this.builder.target(apiType, url);
    }
}
