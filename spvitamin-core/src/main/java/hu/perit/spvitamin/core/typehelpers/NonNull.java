package hu.perit.spvitamin.core.typehelpers;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
@Slf4j
public class NonNull
{
    public <T> Optional<T> get(Supplier<T> supplier)
    {
        try
        {
            return Optional.ofNullable(supplier.get());
        }
        catch (NullPointerException | NoSuchElementException e)
        {
            log.trace("Exception encountered in supplier chain", e);
            return Optional.empty();
        }
    }
}
