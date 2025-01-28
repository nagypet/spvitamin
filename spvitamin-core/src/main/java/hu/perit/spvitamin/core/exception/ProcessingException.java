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

package hu.perit.spvitamin.core.exception;

/**
 * ProcessingException
 * @author Peter Nagy (xgxtpna)
 */
public class ProcessingException extends RuntimeException
{

    private static final long serialVersionUID = 777514113674176800L;

    /**
     * @param message
     * @param cause
     */
    public ProcessingException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ProcessingException(String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public ProcessingException(Throwable cause)
    {
        super(cause);
    }
}
