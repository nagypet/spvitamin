package hu.perit.spvitamin.spring.parallelrunner;

import hu.perit.spvitamin.core.exception.ThrowingRunnable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParallelRunner
{
    private final List<Runnable> runnables = new ArrayList<>();
    private final AsyncContext context = AsyncContext.getContext();

    public void add(ThrowingRunnable runnable)
    {
        this.runnables.add(() -> ContextReplicator.run(context, runnable));
    }

    public void executeAll()
    {
        this.runnables.stream().parallel().forEach(i -> i.run());
    }
}
