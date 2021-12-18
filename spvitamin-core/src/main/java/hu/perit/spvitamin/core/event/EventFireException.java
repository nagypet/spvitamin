package hu.perit.spvitamin.core.event;

import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class EventFireException extends RuntimeException
{
    private final List<Exception> causes;

    public EventFireException(List<Exception> causes)
    {
        super(causes.stream()
                .map(e -> ExceptionWrapper.of(e).getRootCause())
                .map(e -> e.getMessage())
                .collect(Collectors.joining("; ")));
        this.causes = causes;
    }
}
