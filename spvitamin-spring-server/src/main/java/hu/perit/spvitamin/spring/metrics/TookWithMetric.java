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

package hu.perit.spvitamin.spring.metrics;

import hu.perit.spvitamin.core.took.Took;

/**
 * @author Peter Nagy
 */

public class TookWithMetric extends Took
{
    private DualMetric myMetric;
    private MeasurementItem execTimer;

    public TookWithMetric(DualMetric myMetric)
    {
        this.myMetric = myMetric;
        this.myMetric.increment();
        execTimer = new MeasurementItem();
        super.methodName = super.getCallingMethodName("");
    }

    public TookWithMetric(DualMetric myMetric, String context)
    {
        this.myMetric = myMetric;
        this.myMetric.increment();
        execTimer = new MeasurementItem();
        super.methodName = super.getCallingMethodName(context);
    }

    public TookWithMetric(DualMetric myMetric, String context, boolean logAtClose)
    {
        super(logAtClose);
        this.myMetric = myMetric;
        this.myMetric.increment();
        execTimer = new MeasurementItem();
        super.methodName = super.getCallingMethodName(context);
    }

    public void batchSize(Long amount)
    {
        if (amount != null && amount > 1)
        {
            this.myMetric.increment(amount - 1);
        }
    }

    @Override
    public void close()
    {
        super.close();
        this.myMetric.pushPerformance(this.execTimer);
    }
}
