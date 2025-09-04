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
    // Új mezők:
    private final boolean countOnlyOnSuccess;
    private boolean succeeded = false;


    public TookWithMetric(DualMetric myMetric)
    {
        this.myMetric = myMetric;
        this.myMetric.increment();
        execTimer = new MeasurementItem();
        this.countOnlyOnSuccess = false;
        super.methodName = super.getCallingMethodName("");
    }


    public TookWithMetric(DualMetric myMetric, String context)
    {
        this.myMetric = myMetric;
        this.myMetric.increment();
        execTimer = new MeasurementItem();
        this.countOnlyOnSuccess = false;
        super.methodName = super.getCallingMethodName(context);
    }


    public TookWithMetric(DualMetric myMetric, String context, boolean logAtClose)
    {
        super(logAtClose);
        this.myMetric = myMetric;
        this.myMetric.increment();
        execTimer = new MeasurementItem();
        this.countOnlyOnSuccess = false;
        super.methodName = super.getCallingMethodName(context);
    }


    // Új konstruktor: countOnlyOnSuccess vezérli, hogy csak siker esetén mérjünk/számoljunk
    public TookWithMetric(DualMetric myMetric, String context, boolean logAtClose, boolean countOnlyOnSuccess)
    {
        super(logAtClose);
        this.myMetric = myMetric;
        this.execTimer = new MeasurementItem();
        this.countOnlyOnSuccess = countOnlyOnSuccess;
        super.methodName = super.getCallingMethodName(context);

        // VISSZAFELÉ KOMPATIBILITÁS:
        // ha nem kérünk "csak siker" módot, akkor a régi viselkedést tartjuk (azonnali increment)
        if (!this.countOnlyOnSuccess)
        {
            this.myMetric.increment();
        }
    }


    public void batchSize(Long amount)
    {
        if (amount != null && amount > 1)
        {
            this.myMetric.increment(amount - 1);
        }
    }


    // Ezt hívd meg a try-blokk végén, ha minden rendben lefutott
    public void success()
    {
        this.succeeded = true;
    }


    @Override
    public void close()
    {
        try
        {
            if (!this.countOnlyOnSuccess)
            {
                // a régi módot használjuk (már inkrementáltunk a konstruktorban), és az exec időt is hozzá akarod adni
                this.myMetric.pushPerformance(this.execTimer);
            }
            else
            {
                // Csak siker esetén számolunk és rögzítjük az időt
                if (this.succeeded)
                {
                    this.myMetric.increment();
                    this.myMetric.pushPerformance(this.execTimer);
                }
            }
        }
        finally
        {
            super.close();
        }
    }
}
