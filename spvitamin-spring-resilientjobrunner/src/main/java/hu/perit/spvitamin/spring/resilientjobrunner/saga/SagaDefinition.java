package hu.perit.spvitamin.spring.resilientjobrunner.saga;

import hu.perit.spvitamin.core.saga.SagaStep;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobParameter;

import java.util.List;

public interface SagaDefinition<P extends ResilientJobParameter, C extends ResilientJobParameter>
{
    /**
     * == processorType, a runner ezzel azonosítja a feldolgozót.
     */
    Long processorType();

    P loadParameter(String parametersJson, int parameterVersion);

    C loadContext(String contextJson, int contextVersion);

    /**
     * A rendezett lépéssor.
     */
    List<SagaStep<P, C>> steps();

    void persistContext(Long jobId, C context, String lastStep);
}
