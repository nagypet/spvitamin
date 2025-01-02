package hu.perit.spvitamin.spring.parallelrunner;

import lombok.Data;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@Data
public class AsyncContext
{
    private final Map<String, String> threadContext;
    private final SecurityContext securityContext;

    public static AsyncContext getContext()
    {
        return new AsyncContext(ThreadContext.getContext(), SecurityContextHolder.getContext());
    }
}
