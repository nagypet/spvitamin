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

package hu.perit.spvitamin.spring.resilientjobrunner;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

/**
 * Holder for the current resilient job's operationId, stored in the SLF4J MDC.
 * <p>
 * Because the value lives in the MDC, it is automatically captured and restored by
 * {@code AsyncContext.getContext()} / {@code ContextReplicator.setContext()}, so it propagates
 * to child threads without any extra plumbing.
 * <p>
 * Set automatically by the job runner before {@code processor.processJob()} is called.
 * Use {@link #getOperationId()} anywhere in the call stack to retrieve the idempotency key
 * without having to pass it as a method parameter.
 * <p>
 * Usage:
 * <pre>
 *   try (var ctx = ResilientJobContext.bind(entity.getOperationId())) {
 *       UUID opId = ResilientJobContext.getOperationId().orElseThrow();
 *   }
 * </pre>
 */
public final class ResilientJobContext
{
    public static final String MDC_KEY = "operationId";


    private ResilientJobContext()
    {
    }


    /**
     * Puts the given operationId into the MDC and returns a {@link Scope} that,
     * when closed, restores the previous value. Intended for try-with-resources.
     */
    public static Scope bind(UUID operationId)
    {
        return new Scope(operationId);
    }


    /**
     * Returns the operationId from the MDC, or empty if none is set.
     */
    public static Optional<UUID> getOperationId()
    {
        return Optional.ofNullable(MDC.get(MDC_KEY)).map(UUID::fromString);
    }


    public static final class Scope implements AutoCloseable
    {
        private final String previousValue;


        private Scope(UUID operationId)
        {
            this.previousValue = MDC.get(MDC_KEY);
            if (operationId == null)
            {
                MDC.remove(MDC_KEY);
            }
            else
            {
                MDC.put(MDC_KEY, operationId.toString());
            }
        }


        @Override
        public void close()
        {
            if (this.previousValue == null)
            {
                MDC.remove(MDC_KEY);
            }
            else
            {
                MDC.put(MDC_KEY, this.previousValue);
            }
        }
    }
}
