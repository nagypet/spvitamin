package hu.perit.spvitamin.spring.resilientjobrunner.saga;

import hu.perit.spvitamin.spring.resilientjobrunner.ProcessorType;
import hu.perit.spvitamin.spring.resilientjobrunner.service.api.ResilientJobParameter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SagaDefinitionRegistry
{

    private final Map<Long, SagaDefinition<? extends ResilientJobParameter, ? extends ResilientJobParameter>> byProcessor = new HashMap<>();


    public SagaDefinitionRegistry(List<SagaDefinition<?, ?>> definitions)
    {
        definitions.forEach(d -> {
            SagaDefinition<?, ?> prev = byProcessor.put(d.processorType(), d);
            if (prev != null) {
                throw new IllegalStateException("Duplicate saga definition for processor " + d.processorType());
            }
        });
    }


    @SuppressWarnings("unchecked")
    public <P extends ResilientJobParameter, C extends ResilientJobParameter> SagaDefinition<P, C> get(Long processorType)
    {
        SagaDefinition<?, ?> def = byProcessor.get(processorType);
        if (def == null)
        {
            throw new IllegalStateException("No saga definition for processor " + processorType);
        }
        return (SagaDefinition<P, C>) def;
    }
}
