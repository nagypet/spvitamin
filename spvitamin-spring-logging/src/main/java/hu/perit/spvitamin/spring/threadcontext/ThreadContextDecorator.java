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

package hu.perit.spvitamin.spring.threadcontext;

import org.apache.logging.log4j.ThreadContext;

public class ThreadContextDecorator implements AutoCloseable
{
    private final String context;
    private final String oldValue;
    private final ThreadContextDecorator parent;


    public static ThreadContextDecorator with(String batchId, String traceId)
    {
        return new ThreadContextDecorator("batchId", batchId, new ThreadContextDecorator("traceId", traceId));
    }


    public ThreadContextDecorator(String context, String value)
    {
        this.context = context;
        this.oldValue = ThreadContext.get(context);
        this.parent = null;

        ThreadContext.put(context, value);
    }


    public ThreadContextDecorator(String context, String value, String ctxId)
    {
        this.context = context;
        this.oldValue = ThreadContext.get(context);
        this.parent = null;

        ThreadContext.put(context, String.format("%s (%s)", value, ctxId));
    }


    public ThreadContextDecorator(String context, String value, ThreadContextDecorator parent)
    {
        this.context = context;
        this.oldValue = ThreadContext.get(context);
        this.parent = parent;

        ThreadContext.put(context, value);
    }


    @Override
    public void close()
    {
        if (this.parent != null)
        {
            parent.close();
        }
        ThreadContext.put(this.context, oldValue);
    }
}
