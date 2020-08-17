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

package hu.perit.spvitamin.core.connectablecontext;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Peter Nagy
 */


@Log4j
@NoArgsConstructor
public abstract class ConnectableContext
{
    private AtomicBoolean connected = new AtomicBoolean(false);
    private AtomicReference<ContextKey> contextKey = new AtomicReference<>(null);

    private AtomicLong lastActionTime = new AtomicLong(0);
    private AtomicBoolean activityOngoing = new AtomicBoolean(false);

    protected abstract long getIdleTimeout();

    public void connect(ContextKey key)
    {
        if (!this.connected.getAndSet(true))
        {
            this.contextKey.set(key);
            log.debug(String.format("%s.connect(%s)", this.getClass().getSimpleName(), this.getContextName()));
        }
    }


    public boolean isConnected()
    {
        return this.connected.get();
    }


    public void disconnect()
    {
        if (this.connected.getAndSet(false))
        {
            log.debug(String.format("%s.disconnect(%s)", this.getClass().getSimpleName(), this.getContextName()));
        }
    }

    public boolean isIdle()
    {
        if (this.activityOngoing.get())
        {
            // The session should not be removed while activity is ongoing
            return false;
        }

        long elapsed = (System.currentTimeMillis() - this.lastActionTime.get()) / 1000;
        if (elapsed > this.getIdleTimeout())
        {
            log.info(String.format("%s is idle for %d sec: %s", this.getClass().getSimpleName(), this.getIdleTimeout(), this.getContextName()));
            return true;
        }
        else
        {
            return false;
        }
    }


    // This pair of functions should replace the signalActivity. The problem with signalActivity was, that a long running
    // activity might take longer than  the session timeout.
    public void activityStarted()
    {
        this.activityOngoing.set(true);
    }


    public void activityEnded()
    {
        // Allow session removal
        this.activityOngoing.set(false);

        // restart idle timer
        this.lastActionTime.set(System.currentTimeMillis());
    }


    protected String getContextName()
    {
        ContextKey contextKeyRef = this.contextKey.get();
        return contextKeyRef != null ? contextKeyRef.toString() : "null";
    }

    @Override
    public String toString()
    {
        return "ConnectableContext{" +
                "connected=" + connected +
                ", isIdle=" + this.isIdle() +
                ", contextKey=" + contextKey +
                ", lastActionTime=" + lastActionTime +
                '}';
    }
}
