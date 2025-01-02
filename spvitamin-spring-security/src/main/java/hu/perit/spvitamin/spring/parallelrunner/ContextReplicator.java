package hu.perit.spvitamin.spring.parallelrunner;

import hu.perit.spvitamin.core.exception.CheckedExceptionConverter;
import hu.perit.spvitamin.core.exception.ThrowingRunnable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextReplicator
{
    public static void setContext(AsyncContext context)
    {
        ThreadContext.putAll(context.getThreadContext());
        SecurityContextHolder.setContext(context.getSecurityContext());
    }


    public static void run(AsyncContext context, ThrowingRunnable runnable)
    {
        CheckedExceptionConverter.invokeVoid(() -> {
                    setContext(context);
                    runnable.run();
                }
        );
    }
}
