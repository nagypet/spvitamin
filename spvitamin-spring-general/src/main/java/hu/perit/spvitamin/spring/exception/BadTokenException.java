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

package hu.perit.spvitamin.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, code = HttpStatus.BAD_REQUEST)
public class BadTokenException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = -6596296814497856887L;

    public BadTokenException(String message)
    {
        super(message);
    }

    public BadTokenException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadTokenException(Throwable cause)
    {
        super(cause);
    }
}
