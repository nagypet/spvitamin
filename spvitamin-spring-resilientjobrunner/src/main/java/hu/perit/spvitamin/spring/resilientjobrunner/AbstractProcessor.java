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

import hu.perit.spvitamin.spring.resilientjobrunner.config.ResilientJobProperties;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractProcessor
{
    private final ProcessorType processorType;
    private final ResilientJobProperties properties;


    // In some rare circumstances this method might be called multiple times for the same job, even if the job has been
    // processed successfully. The processor should be idempotent (use operationId as idempotency key).
    public abstract void processJob(AbstractResilientJobEntity entity) throws Exception;

    public abstract void onError(AbstractResilientJobEntity entity, Exception e);


    /**
     * If the exception is retryable, we will retry the job once again. For instance, FeignException.ServiceUnavailable.
     */
    public boolean isRetryableException(Throwable e)
    {
        return ExceptionHelper.isRetryableException(e, this.properties.getRetryableExceptions());
    }


    public boolean isItemRelatedException(Throwable e)
    {
        return ExceptionHelper.isItemRelatedException(e, this.properties.getItemRelatedExceptions());
    }
}
