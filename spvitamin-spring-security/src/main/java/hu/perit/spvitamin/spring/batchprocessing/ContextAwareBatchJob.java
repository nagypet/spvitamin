package hu.perit.spvitamin.spring.batchprocessing;

import hu.perit.spvitamin.core.batchprocessing.BatchJob;
import hu.perit.spvitamin.spring.parallelrunner.AsyncContext;
import hu.perit.spvitamin.spring.parallelrunner.ContextReplicator;

public abstract class ContextAwareBatchJob extends BatchJob
{
    private final AsyncContext asyncContext = AsyncContext.getContext();


    @Override
    protected void setUp()
    {
        ContextReplicator.setContext(this.asyncContext);
    }
}
