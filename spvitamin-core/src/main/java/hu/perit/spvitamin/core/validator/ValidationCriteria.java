package hu.perit.spvitamin.core.validator;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.function.Predicate;

@Data
public class ValidationCriteria<T>
{
    @Getter(AccessLevel.NONE)
    private final Predicate<T> predicate;
    private final String reason;


    public boolean succeeds(T t)
    {
        return this.predicate.test(t);
    }


    public boolean fails(T t)
    {
        return !succeeds(t);
    }
}
