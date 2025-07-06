package hu.perit.spvitamin.spring.threadcontext;

import org.apache.logging.log4j.ThreadContext;

public class ThreadContextDecorator implements AutoCloseable
{
    private final String context;
    private final String oldValue;


    public ThreadContextDecorator(String context, String value)
    {
        this.context = context;
        this.oldValue = ThreadContext.get(context);

        ThreadContext.put(context, value);
    }


    public ThreadContextDecorator(String context, String value, String ctxId)
    {
        this.context = context;
        this.oldValue = ThreadContext.get(context);

        ThreadContext.put(context, String.format("%s (%s)", value, ctxId));
    }


    @Override
    public void close()
    {
        ThreadContext.put(this.context, oldValue);
    }
}
