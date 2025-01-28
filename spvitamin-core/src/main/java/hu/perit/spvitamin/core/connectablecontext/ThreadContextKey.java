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

package hu.perit.spvitamin.core.connectablecontext;

import lombok.Getter;

/**
 * @author Peter Nagy
 */


@Getter
public class ThreadContextKey extends GenericContextKey<String>
{
    public ThreadContextKey()
    {
        this.keyValue = Thread.currentThread().getName();
    }

    @Override
    public boolean isInvalid()
    {
        if (this.keyValue == null) return true;
        if (this.getThreadByName(this.keyValue) == null) return true;
        return !this.getThreadByName(this.keyValue).isAlive(); // NOSONAR
    }

    private Thread getThreadByName(String threadName)
    {
        for (Thread t : Thread.getAllStackTraces().keySet())
        {
            if (t.getName().equals(threadName)) return t;
        }
        return null;
    }
}
