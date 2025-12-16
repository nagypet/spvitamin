package hu.perit.spvitamin.spring.jobexecutor;

import hu.perit.spvitamin.core.jobexecutor.CancellableJob;
import hu.perit.spvitamin.spring.parallelrunner.AsyncContext;
import hu.perit.spvitamin.spring.parallelrunner.ContextReplicator;

public abstract class ContextAwareCancellableJob extends CancellableJob
{
    private final AsyncContext asyncContext = AsyncContext.getContext();


    @Override
    protected void setUp()
    {
        ContextReplicator.setContext(this.asyncContext);
    }
}
