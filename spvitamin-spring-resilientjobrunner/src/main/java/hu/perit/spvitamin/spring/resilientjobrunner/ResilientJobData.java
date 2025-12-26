package hu.perit.spvitamin.spring.resilientjobrunner;

import java.time.OffsetDateTime;

public interface ResilientJobData
{
    Long getId();

    OffsetDateTime getCreationTimestamp();

    ResilientJobStatus getStatus();

    Long getProcessorType();

    Integer getParameterVersion();

    OffsetDateTime getProcessingStartedTimestamp();

    String getErrorText();

    Long getRetryCount();

    <T> T getParameters(Class<T> clazz);
}
