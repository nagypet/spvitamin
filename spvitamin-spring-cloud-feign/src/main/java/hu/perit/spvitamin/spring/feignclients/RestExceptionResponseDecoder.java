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

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import hu.perit.spvitamin.spring.json.JsonSerializable;
import lombok.extern.log4j.Log4j;

import java.io.IOException;

/**
 * @author Peter Nagy
 */

@Log4j
public class RestExceptionResponseDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader());
                RestExceptionResponse exceptionResponse = JsonSerializable.fromJson(body, RestExceptionResponse.class);
                Exception ex = this.getException(exceptionResponse);
                return ex != null ? ex : FeignException.errorStatus(methodKey, response);
            }
        }
        catch (IOException ex) {
            log.error(StackTracer.toString(ex));
        }

        return FeignException.errorStatus(methodKey, response);
    }


    private Exception getException(RestExceptionResponse exceptionResponse) {
        if (exceptionResponse != null && exceptionResponse.getException() != null) {
            return exceptionResponse.getException().toException();
        }
        return null;
    }

}
