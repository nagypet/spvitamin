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

package hu.perit.spvitamin.spring.metrics;

import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;

/**
 * @author Peter Nagy
 */

@Log4j
public class SingleMetricAverageOfLastNValueGeneric<T extends Number>
{
    private static int myQueueSize = 50;
    public static int getQueueSize()
    {
        return myQueueSize;
    }

    private CircularFifoQueue<T> queue;

    public SingleMetricAverageOfLastNValueGeneric()
    {
        this.queue = new CircularFifoQueue(myQueueSize);
    }


    public synchronized Double getAverage()
    {
        Iterator<T> iter = queue.iterator();
        BigDecimal sum = new BigDecimal(0);
        while (iter.hasNext())
        {
            T item = iter.next();
            sum = sum.add(new BigDecimal(item.doubleValue()));
        }

        if (!queue.isEmpty())
        {
            return sum.divide(new BigDecimal(queue.size()), 2, RoundingMode.HALF_UP).doubleValue();
        }
        else
        {
            return 0.0;
        }
    }


    public synchronized Double getMax()
    {
        return this.queue.stream().mapToDouble(Number::doubleValue).max().orElse(0);
    }


    public synchronized Double getMin()
    {
        return this.queue.stream().mapToDouble(Number::doubleValue).min().orElse(0);
    }


    public void push(T value)
    {
        synchronized (this)
        {
            queue.add(value);
        }
    }
}
