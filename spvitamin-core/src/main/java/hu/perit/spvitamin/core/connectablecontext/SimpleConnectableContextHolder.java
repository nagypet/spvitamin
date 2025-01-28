
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

import java.util.Iterator;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */


@Slf4j
public abstract class SimpleConnectableContextHolder<T extends ConnectableContext>
{
    protected abstract ConnectableContextCollection<T> getContextCollection();

    // CUS-8859: Nagy terheles beindulasakor, mivel a getContext() synchronized volt, ezert a context.connect() hivasok csak
    // egymas utan tudtak lefutni. Ez gondot okoz, mert a szalak felfutese indokolatlanul lassan tortenik. Ezert a
    // context.connect() hivast kiveszem a synchronized blockbol, hogy tobb szalon is parhuzamosan futhasson.
    public T getContext(ContextKey key)
    {
        T context = null;
        synchronized (this)
        {
            context = this.getContextCollection().get(key);
        }

        synchronized (context)
        {
            if (!context.isConnected())
            {
                // Ez egy lassú hívás, néha akár 3 mp-ig is eltarthat
                context.connect(key);
            }
        }

        return context;
    }


    public synchronized void cleanup(boolean onlyIdleContexts)
    {
        ConnectableContextCollection<T> contextCollection = this.getContextCollection();
        boolean hasChanged = false;
        try
        {
            Iterator<ContextKey> iter = contextCollection.keySet().iterator();
            while (iter.hasNext())
            {
                ContextKey key = iter.next();
                ConnectableContext context = contextCollection.get(key);
                if (context != null)
                {
                    // - vagy végső lebontás
                    // - vagy connected és idle
                    // - vagy megdöglött a thread
                    if (!onlyIdleContexts || context.isIdle() || key.isInvalid())
                    {
                        try
                        {
                            if (context.isConnected())
                            {
                                context.disconnect();
                            }
                            hasChanged = true;
                            iter.remove();
                        }
                        catch (RuntimeException ex)
                        {
                            log.error(StackTracer.toString(ex));
                        }
                    }
                }
            }
        }
        finally
        {
            if (hasChanged)
            {
                log.debug(String.format("Count of active %s contexts: %d", this.getContextCollection().getContextTypeName(), this.getContextCollection().size()));
            }
        }
    }


    public void cleanup()
    {
        log.debug("cleanup");
        this.cleanup(false);
    }


    public synchronized int size()
    {
        return this.getContextCollection().size();
    }
}
