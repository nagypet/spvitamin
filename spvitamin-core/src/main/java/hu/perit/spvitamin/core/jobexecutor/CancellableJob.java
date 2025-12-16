package hu.perit.spvitamin.core.jobexecutor;

import java.util.concurrent.Callable;

public abstract class CancellableJob  implements Callable<Void>
{
    @Override
    public Void call() throws Exception
    {
        setUp();
        return execute();
    }


    protected void setUp()
    {

    }

    protected abstract Void execute() throws Exception; // NOSONAR
}
