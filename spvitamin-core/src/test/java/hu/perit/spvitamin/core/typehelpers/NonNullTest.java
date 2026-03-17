package hu.perit.spvitamin.core.typehelpers;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NonNullTest
{
    @Test
    void get_shouldReturnValue_whenSupplierReturnsNonNull()
    {
        Optional<String> result = NonNull.get(() -> "value");

        assertThat(result).hasValue("value");
    }


    @Test
    void get_shouldReturnEmpty_whenSupplierReturnsNull()
    {
        Optional<String> result = NonNull.get(() -> null);

        assertThat(result).isEmpty();
    }


    @Test
    void get_shouldReturnEmpty_whenSupplierThrowsNullPointerException()
    {
        Optional<Object> result = NonNull.get(() -> {
            Object o = null;
            // Simulate NPE in a supplier chain
            return o.toString();
        });

        assertThat(result).isEmpty();
    }


    @Test
    void get_shouldPropagateOtherExceptions_whenSupplierThrowsNonNPE()
    {
        assertThatThrownBy(() ->
                NonNull.get(() -> {
                    throw new IllegalStateException("boom");
                })
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }
}
