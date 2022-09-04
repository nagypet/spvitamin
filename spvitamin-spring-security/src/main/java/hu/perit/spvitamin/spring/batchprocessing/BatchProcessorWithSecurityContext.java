package hu.perit.spvitamin.spring.batchprocessing;

import hu.perit.spvitamin.core.batchprocessing.BatchProcessor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BatchProcessorWithSecurityContext extends BatchProcessor
{
    public BatchProcessorWithSecurityContext(int threadPoolSize)
    {
        super(threadPoolSize);
    }

    @Override
    protected ExecutorService createExecutorService()
    {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(threadPoolSize), SecurityContextHolder.getContext());
    }
}
