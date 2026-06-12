package hu.perit.spvitamin.core.validator;

@FunctionalInterface
public interface ThrowingFunction<T, E extends Exception>
{
    E apply(T result) throws E;
}
