package hu.perit.spvitamin.core.singleton;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Thread-safe lazy initializer using the initialization-on-demand holder idiom.
 *
 * @param <T> The type of the lazily initialized object
 */
@RequiredArgsConstructor
public class SingletonFactory<T>
{
    private final Supplier<T> supplier;
    private final AtomicReference<T> atomicReference = new AtomicReference<>();


    public static <T> SingletonFactory<T> of(Supplier<T> supplier)
    {
        return new SingletonFactory<>(supplier);
    }


    public boolean isNull()
    {
        return atomicReference.get() == null;
    }


    public boolean isNotNull()
    {
        return !isNull();
    }


    public T getInstance()
    {
        T value = atomicReference.get();
        if (value == null)
        {
            T newValue = supplier.get();
            if (atomicReference.compareAndSet(null, newValue))
            {
                value = newValue;
            }
            else
            {
                value = atomicReference.get();
            }
        }
        return value;
    }
}
