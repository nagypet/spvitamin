package hu.perit.spvitamin.spring.resilientjobrunner.saga;

import hu.perit.spvitamin.core.saga.SagaRunner;
import hu.perit.spvitamin.core.typehelpers.IntUtils;
import hu.perit.spvitamin.spring.resilientjobrunner.db.entity.AbstractResilientJobEntity;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SagaExecutor
{
    private final SagaDefinitionRegistry registry;          // processorType -> SagaDefinition


    /**
     * A ResilientJob feldolgozása. Megszakítás után onnan folytatódik, ahol abbamaradt.
     * Bármely lépés hibája kivételként száll fel -> a ResilientJobRunner retry-ol -> újra hívódik.
     * Ha a retry-ok kimerülnek és a job véglegesen hibásodik, a kompenzáció is lefut.
     */
    public <P extends ResilientJobParameter, C extends ResilientJobParameter> void execute(AbstractResilientJobEntity job) throws Exception
    {
        SagaDefinition<P, C> saga = registry.get(job.getProcessorType());

        C context = saga.loadContext(job.getSagaContext(), IntUtils.get(job.getSagaContextVersion()));
        P parameter = saga.loadParameter(job.getParameters(), IntUtils.get(job.getParameterVersion()));

        // KRITIKUS: minden lépés után önálló tranzakcióban commitolunk,
        // hogy a haladás túléljen egy későbbi lépés hibáját.
        SagaRunner<P, C> runner = new SagaRunner<>(
                saga.steps(),
                (ctx, stepName) -> saga.persistContext(job.getId(), ctx, stepName)
        );

        runner.execute(parameter, context);

        log.info("Saga completed for job {}", job.getId());
    }
}
