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

package hu.perit.spvitamin.spring.tokencache;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.feignclients.RestExceptionResponseDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author Peter Nagy
 */

@RequiredArgsConstructor
public class UnauthorizedHandlingResponseDecoder<T> implements ErrorDecoder
{
    private final T system;
    private final ErrorDecoder defaultErrorDecoder = new RestExceptionResponseDecoder();


    @Override
    public Exception decode(String methodKey, Response response)
    {
        HttpStatus httpStatus = HttpStatus.resolve(response.status());
        if (httpStatus == HttpStatus.UNAUTHORIZED)
        {
            // Removing the invalid, cached token so that a new authorization will be tried
            TokenCache<T> tokenCache = SpringContext.getBean(TokenCache.class);
            tokenCache.clearCachedToken(system);
            // Returning a retryable exception so that the authentication will be tried again
            return new RetryableException(
                    response.status(),
                    response.reason(),
                    response.request().httpMethod(),
                    this.defaultErrorDecoder.decode(methodKey, response),
                    (Long) null,
                    response.request()
            );
        }

        return this.defaultErrorDecoder.decode(methodKey, response);
    }
}
