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

import hu.perit.spvitamin.spring.config.MetricsProperties;
import hu.perit.spvitamin.spring.config.SysConfig;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Peter Nagy
 */

@Log4j
public class SingleMetricExecutionPerformance
{
    private static int myQueueSize = 10;
    public static int getQueueSize()
    {
        return myQueueSize;
    }

    private CircularFifoQueue<MeasurementItem> queue;

    public SingleMetricExecutionPerformance()
    {
        MetricsProperties metricsProperties = SysConfig.getMetricsProperties();
        myQueueSize = metricsProperties.getPerformanceItemcount();
        this.queue = new CircularFifoQueue<>(myQueueSize);
    }


    public synchronized Double getPerformance()
    {
        List<MeasurementSession> sessions = new ArrayList<>();

        Iterator<MeasurementItem> iter = queue.iterator();
        while (iter.hasNext())
        {
            MeasurementItem item = iter.next();
            this.insertIntoSessions(sessions, item);
        }

        double duration = 0;
        int count = 0;
        for (MeasurementSession session : sessions)
        {
            log.debug(session.toString());
            duration += session.getDuration();
            count += session.getCount();
        }
        if (count == 0) return 0.0;
        return duration / count;
    }


    public synchronized Double getAverage()
    {
        Iterator<MeasurementItem> iter = queue.iterator();
        BigDecimal sum = new BigDecimal(0);
        while (iter.hasNext())
        {
            MeasurementItem item = iter.next();
            sum = sum.add(new BigDecimal(item.getDuration()));
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
        return (double) this.queue.stream().mapToLong(MeasurementItem::getDuration).max().orElse(0);
    }


    public synchronized Double getMin()
    {
        return (double) this.queue.stream().mapToLong(MeasurementItem::getDuration).min().orElse(0);
    }


    private void insertIntoSessions(List<MeasurementSession> sessions, MeasurementItem item)
    {
        Iterator<MeasurementSession> iter = sessions.iterator();
        boolean added = false;
        while (iter.hasNext())
        {
            MeasurementSession session = iter.next();
            if (session.isOverlapping(item) && !added)
            {
                // csak egy session-höz adjuk hozzá
                session.addItem(item);
                added = true;
            }
        }

        if (!added)
        {
            MeasurementSession session = new MeasurementSession();
            session.addItem(item);
            sessions.add(session);
        }

        // Az átlapoló session-ök mördzsölése
        for (int i = 0; i < sessions.size(); i++)
        {
            for (int j = i + 1; j < sessions.size(); j++)
            {
                MeasurementSession session1 = sessions.get(i);
                MeasurementSession session2 = sessions.get(j);
                if (session1.isOverlapping(session2))
                {
                    session2.merge(session1);
                    sessions.remove(i);
                    break;
                }
            }
        }
    }


    public void push(MeasurementItem execTime)
    {
        execTime.stop();
        synchronized (this)
        {
            queue.add(execTime);
        }
    }
}
