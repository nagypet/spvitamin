package hu.perit.spvitamin.spring.resilientjobrunner.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class ResilientJobProperties
{
    @NotNull
    private Long id;
    @NotNull
    private String processorClass;
    private int threadPoolSize = 10;
    private Duration retryTimeout = Duration.ofHours(24);
    private String contextDecoratorTag = "batchId";
    @NotEmpty
    private List<String> retryableExceptions;
    @NotEmpty
    private List<String> itemRelatedExceptions;
}
