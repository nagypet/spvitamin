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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * @author Peter Nagy
 */

public class DualMetric
{
    private final Counter counter;
    private SingleMetricExecutionPerformance executionPerformance = new SingleMetricExecutionPerformance();

    public DualMetric(MeterRegistry registry, String componentName, String functionName)
    {
        this.counter = registry.counter(this.getCounterName(componentName, functionName));

        Gauge
                .builder(this.getPerformanceGaugeName(componentName, functionName), this.executionPerformance, SingleMetricExecutionPerformance::getPerformance)
                .description(String.format("The performance of last %d %s operations", SingleMetricExecutionPerformance.getQueueSize(), functionName))
                .baseUnit("ms")
                .register(registry);

        Gauge
                .builder(this.getExecTimeGaugeName(componentName, functionName), this.executionPerformance, SingleMetricExecutionPerformance::getAverage)
                .description(String.format("The average execution time of last %d %s operations", SingleMetricExecutionPerformance.getQueueSize(), functionName))
                .baseUnit("ms")
                .register(registry);

    }

    private String getCounterName(String componentName, String functionName)
    {
        return String.format("%s.count.%s", componentName, functionName);
    }

    private String getPerformanceGaugeName(String componentName, String functionName)
    {
        return String.format("%s.performance.%s", componentName, functionName);
    }

    private String getExecTimeGaugeName(String componentName, String functionName)
    {
        return String.format("%s.avgexecutiontime.%s", componentName, functionName);
    }

    public void increment()
    {
        this.counter.increment();
    }

    /**
     * This is used for batch processing, where more then one item has been processed within a given amount of time.
     *
     * @param amount
     */
    public void increment(Long amount)
    {
        if (amount != null)
        {
            this.counter.increment(amount);
        }
    }

    public void pushPerformance(MeasurementItem execTime)
    {
        this.executionPerformance.push(execTime);
    }
}
