package hu.perit.spvitamin.core.exception;

import hu.perit.spvitamin.core.StackTracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionGuard
{
    public static void withTryCatch(Runnable r)
    {
        try
        {
            r.run();
        }
        catch (Exception e)
        {
            log.error(StackTracer.toStringCompact(e));
        }
    }
}
