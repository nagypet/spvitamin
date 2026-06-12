package hu.perit.spvitamin.core.validator;

import java.io.Serial;
import java.util.List;

public class ValidationException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final List<String> reasons;


    public ValidationException(List<String> reasons)
    {
        super(String.join(", ", reasons));
        this.reasons = reasons;
    }
}
