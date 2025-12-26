package hu.perit.spvitamin.spring.resilientjobrunner.privates;

import hu.perit.spvitamin.core.batchprocessing.BatchProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.AbstractProcessor;
import hu.perit.spvitamin.spring.resilientjobrunner.ResilientJobData;

class BatchExecutor extends BatchProcessor
{
    private final AbstractProcessor processor;


    public BatchExecutor(int threadPoolSize, AbstractProcessor processor)
    {
        super(threadPoolSize);
        this.processor = processor;
    }


    public BJob createBJob(ResilientJobData entity)
    {
        return new BJob(entity, processor);
    }
}
