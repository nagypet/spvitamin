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
import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonEncoder;
import feign.optionals.OptionalDecoder;
import hu.perit.spvitamin.spring.config.FeignProperties;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.feignclients.cookie.CookieHandlingFeignClient;
import hu.perit.spvitamin.spring.feignclients.cookie.CookieRequestInterceptor;
import hu.perit.spvitamin.spring.feignclients.cookie.CookieStoreService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Peter Nagy
 */

public class SimpleFeignClientBuilder
{
    private final Feign.Builder builder;
    private final RequestInterceptorAdapter requestInterceptorAdapter = new RequestInterceptorAdapter();

    private Encoder encoder;
    private Decoder decoder;
    private ErrorDecoder errorDecoder = new RestExceptionResponseDecoder();
    private Retryer retryer;
    private Client client;
    private boolean exposeSetCookieHeaders = false;
    private boolean browserMode = false;
    private String clientId;


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

        // Encoder
        this.encoder = new JacksonEncoder(objectMapper); // default encoder

        // Decoder
        ObjectFactory<HttpMessageConverters> messageConverters = () -> new HttpMessageConverters(new ByteArrayHttpMessageConverter());
        this.decoder = new OptionalDecoder(
                new ResponseEntityDecoder(
                        new SpringDecoder(messageConverters)
                )
        );

        // Retryer
        this.retryer = new Retryer.Default(
                feignProperties.getRetry().getPeriod(),
                feignProperties.getRetry().getMaxPeriod(),
                feignProperties.getRetry().getMaxAttempts()
        );

        // Base client
        this.client = getClient();

        this.builder = Feign.builder()
                .contract(new SpringMvcContract())
                .requestInterceptor(this.requestInterceptorAdapter)
                .logLevel(getLevel(feignProperties.getLoggerLevel()));
    }


    Client getClient()
    {
        return new Client.Default(null, null);
    }


    public SimpleFeignClientBuilder withMultipartEncoder()
    {
        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);
        List<HttpMessageConverter<?>> converters = new RestTemplate().getMessageConverters();
        converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        this.encoder = new SpringFormEncoder(new SpringEncoder(() -> new HttpMessageConverters(converters)));
        return this;
    }


    public SimpleFeignClientBuilder encoder(Encoder encoder)
    {
        this.encoder = encoder;
        return this;
    }


    public SimpleFeignClientBuilder decoder(Decoder decoder)
    {
        this.decoder = decoder;
        return this;
    }


    public SimpleFeignClientBuilder errorDecoder(ErrorDecoder errorDecoder)
    {
        this.errorDecoder = errorDecoder;
        return this;
    }


    public SimpleFeignClientBuilder retryer(Retryer retryer)
    {
        this.retryer = retryer;
        return this;
    }


    public SimpleFeignClientBuilder client(Client client)
    {
        this.client = client;
        return this;
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


    public SimpleFeignClientBuilder exposeSetCookieHeaders(boolean allowCookies)
    {
        this.exposeSetCookieHeaders = allowCookies;
        return this;
    }


    public SimpleFeignClientBuilder browserModeWithClientId(String clientId)
    {
        this.browserMode = true;
        this.clientId = clientId;
        return this;
    }


    public <T> T build(Class<T> apiType, String url)
    {
        this.builder.encoder(this.encoder);
        this.builder.decoder(this.decoder);
        this.builder.errorDecoder(this.errorDecoder);
        this.builder.retryer(this.retryer);
        this.builder.logger(new ClientIdAwareSlf4jLogger(getClass(), this.clientId));

        Client effectiveClient = this.client;
        if (this.browserMode)
        {
            if (StringUtils.isBlank(this.clientId))
            {
                throw new RuntimeException("clientId is required in browser mode!");
            }
            CookieStoreService cookieStoreService = SpringContext.getBean(CookieStoreService.class);
            effectiveClient = new CookieHandlingFeignClient(effectiveClient, cookieStoreService, this.clientId);
            this.requestInterceptorAdapter.addInterceptor(new CookieRequestInterceptor(cookieStoreService, this.clientId));
        }

        if (!this.exposeSetCookieHeaders)
        {
            effectiveClient = new HeaderFilterFeignClient(effectiveClient);
        }

        this.builder.client(effectiveClient);
        return this.builder.target(apiType, url);
    }
}
