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

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import hu.perit.spvitamin.spring.json.JsonSerializable;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Peter Nagy
 */

public class RestExceptionResponseDecoder implements ErrorDecoder
{
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response)
    {
        HttpStatus httpStatus = HttpStatus.resolve(response.status());
        if (httpStatus != null && httpStatus.isError())
        {
            if (response.body() != null)
            {
                try
                {
                    String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                    RestExceptionResponse exceptionResponse = JsonSerializable.fromJson(body, RestExceptionResponse.class);
                    Exception ex = this.getException(exceptionResponse);
                    if (ex != null)
                    {
                        return ex;
                    }
                }
                catch (IOException ex)
                {
                    // Error response could not be converted into a RestExceptionResponse
                }
            }
        }

        return this.defaultErrorDecoder.decode(methodKey, response);
    }


    private Exception getException(RestExceptionResponse exceptionResponse)
    {
        if (exceptionResponse != null && exceptionResponse.getException() != null)
        {
            return exceptionResponse.getException().toException();
        }
        return null;
    }

}
