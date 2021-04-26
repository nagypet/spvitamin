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

package hu.perit.spvitamin.spring.resttemplate;

import hu.perit.spvitamin.core.exception.ServerException;
import hu.perit.spvitamin.spring.exceptionhandler.RestExceptionResponse;
import hu.perit.spvitamin.spring.json.JsonSerializable;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        Exception ex = this.getException(response);
        if (ex instanceof RuntimeException) {
            throw (RuntimeException)ex;
        }
        else {
            ServerException.throwFrom(ex);
        }
    }


    private Exception getException(ClientHttpResponse response) throws IOException {
        Charset charset = getCharset(response);
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        response.getBody();
        String bodyAsText = StreamUtils.copyToString(response.getBody(), charset);
        RestExceptionResponse exceptionResponse = JsonSerializable.fromJson(bodyAsText, RestExceptionResponse.class);
        return this.getException(exceptionResponse);
    }


    private Exception getException(RestExceptionResponse exceptionResponse) {
        if (exceptionResponse != null && exceptionResponse.getException() != null) {
            return exceptionResponse.getException().toException();
        }
        return null;
    }
}
