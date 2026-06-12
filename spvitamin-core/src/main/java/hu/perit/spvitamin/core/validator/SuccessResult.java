package hu.perit.spvitamin.core.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SuccessResult
{
    private final boolean success;
    private final List<String> successReasons;

    public boolean hasErrors()
    {
        return !success;
    }


    public String getSuccessReasonsAsString()
    {
        return String.join(", ", successReasons);
    }
}
