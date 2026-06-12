/*
 * Copyright 2020-2025 the original author or authors.
 */

package hu.perit.spvitamin.core.saga;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;

/**
 * Standalone Saga végrehajtó. Nincs Spring függőség, nincs DB entitás.
 * <p>
 * Két módban használható:
 * <ul>
 *   <li><b>In-memory mód</b>: {@code new SagaRunner<>(steps)} — csak kompenzáció, nincs perzisztálás</li>
 *   <li><b>Durable mód</b>: {@code new SagaRunner<>(steps, persistContext)} — minden lépés után
 *       a {@code persistContext} callback menti az állapotot (pl. DB)</li>
 * </ul>
 * <p>
 * Ha egy lépés kivételt dob, a már sikeresen elvégzett lépések {@code compensate()} metódusa
 * fordított sorrendben hívódik meg.
 */
@Slf4j
public class SagaRunner<P, C>
{
    private final List<SagaStep<P, C>> steps;
    private final BiConsumer<C, String> persistContext;

    public SagaRunner(List<SagaStep<P, C>> steps)
    {
        this(steps, null);
    }

    public SagaRunner(List<SagaStep<P, C>> steps, BiConsumer<C, String> persistContext)
    {
        this.steps = steps;
        this.persistContext = persistContext;
    }

    public void execute(P parameter, C context) throws Exception
    {
        List<SagaStep<P, C>> completed = new ArrayList<>();

        try
        {
            for (SagaStep<P, C> step : steps)
            {
                if (step.isCompleted(context))
                {
                    completed.add(step);
                    continue;
                }

                if (!step.reconcile(parameter, context))
                {
                    log.info("Executing saga step '{}'", step.getClass().getSimpleName());
                    step.execute(parameter, context);
                }
                else
                {
                    log.info("Saga step '{}' already completed (reconciled)", step.getClass().getSimpleName());
                }

                completed.add(step);

                if (persistContext != null)
                {
                    persistContext.accept(context, step.getClass().getSimpleName());
                }
            }

            log.info("Saga completed successfully ({} steps)", completed.size());
        }
        catch (Exception e)
        {
            log.error("Saga step failed, starting compensation for {} completed step(s)", completed.size());
            compensate(parameter, context, completed);
            throw e;
        }
    }

    private void compensate(P parameter, C context, List<SagaStep<P, C>> completed)
    {
        ListIterator<SagaStep<P, C>> it = completed.listIterator(completed.size());
        while (it.hasPrevious())
        {
            SagaStep<P, C> step = it.previous();
            try
            {
                log.info("Compensating saga step '{}'", step.getClass().getSimpleName());
                step.compensate(parameter, context);
            }
            catch (Exception ce)
            {
                log.error("Compensation failed for step '{}': {}", step.getClass().getSimpleName(), ce.getMessage(), ce);
            }
        }
    }
}
