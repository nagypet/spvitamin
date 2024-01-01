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

package hu.perit.spvitamin.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The @ResponseStatus annotation is used in hu.perit.spvitamin.spring.exceptionhandler.RestResponseEntityExceptionHandler
 *
 * @author Peter Nagy
 */


@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, code = HttpStatus.SERVICE_UNAVAILABLE)
public class CannotConnectException extends RuntimeException {

    public CannotConnectException(String message) {
        super(message);
    }

    public CannotConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
