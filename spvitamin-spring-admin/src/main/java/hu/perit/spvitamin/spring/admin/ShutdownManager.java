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

package hu.perit.spvitamin.spring.admin;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Peter Nagy
 */


@Component
@Log4j
public class ShutdownManager extends Thread
{
    @Autowired
    private ApplicationContext appContext;

    @Override
    public void run()
    {
        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            log.error(StackTracer.toString(e));
        }
        this.initiateShutdown(0);
    }

    private void initiateShutdown(int returnCode)
    {
        SpringApplication.exit(appContext, () -> returnCode);
    }
}