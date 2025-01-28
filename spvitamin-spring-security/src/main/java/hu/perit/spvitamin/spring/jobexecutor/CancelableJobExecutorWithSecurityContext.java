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

package hu.perit.spvitamin.spring.jobexecutor;

import hu.perit.spvitamin.core.jobexecutor.CancelableJobExecutor;
import hu.perit.spvitamin.spring.config.SpringContext;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;

/**
 * @author Peter Nagy
 */

@Slf4j
public class CancelableJobExecutorWithSecurityContext<T> extends CancelableJobExecutor<T>
{

    public CancelableJobExecutorWithSecurityContext(int poolSize, String context)
    {
        super(poolSize, context);
    }

    public Future<Void> submitJob(T id, CancelableJobWithSecurityContext job)
    {
        // Setting the AuthenticatedUser
        AuthorizationService authorizationService = SpringContext.getBean(AuthorizationService.class);
        job.setAuthenticatedUser(authorizationService.getAuthenticatedUser());

        return super.submitJob(id, job);
    }
}
