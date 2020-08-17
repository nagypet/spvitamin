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

package hu.perit.spvitamin.spring.connectablecontext;

import hu.perit.spvitamin.core.connectablecontext.ActivityLock;
import hu.perit.spvitamin.core.connectablecontext.ConnectableContext;
import hu.perit.spvitamin.core.connectablecontext.ContextKey;
import lombok.extern.log4j.Log4j;

/**
 * @author Peter Nagy
 */

@Log4j
public class ThreadSpecificContext extends ConnectableContext
{
    private static final int IDLETIMESEC = 2;

    public ThreadSpecificContext()
    {
    }

    @Override
    protected long getIdleTimeout()
    {
        return IDLETIMESEC;
    }


    @Override
    public void connect(ContextKey key)
    {
        try (ActivityLock lock = new ActivityLock(this)) {
            super.connect(key);
        }
    }

    @Override
    public boolean isIdle()
    {
        return super.isIdle();
    }

    @Override
    public void disconnect()
    {
        log.debug(String.format("Cleaning up '%s'...", getContextName()));

        super.disconnect();
    }

    @Override
    public String toString()
    {
        return super.toString();
    }
}
