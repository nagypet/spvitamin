package hu.perit.spvitamin.spring.resilientjobrunner;

import lombok.Data;

@Data
public class ProcessorType
{
    private final String name;
    private final long processorId; // For storing in the database

    public static ProcessorType of(String name, long value)
    {
        return new ProcessorType(name, value);
    }
}
