package hu.perit.spvitamin.core.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FailureResult
{
    private final boolean success;
    private final List<String> failureReasons;


    public boolean hasErrors()
    {
        return !success;
    }


    public String getFailureReasonsAsString()
    {
        return String.join(", ", failureReasons);
    }
}
