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

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

public class InvalidTokenException extends AuthenticationException
{

    @Serial
    private static final long serialVersionUID = 4357982974622912896L;


    public InvalidTokenException(String msg, Throwable t)
    {
        super(msg, t);
    }


    public InvalidTokenException(String msg)
    {
        super(msg);
    }
}
