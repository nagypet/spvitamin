package hu.perit.spvitamin.core.typehelpers;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Null-safe utility methods for {@link BigDecimal} arithmetic and comparison.
 *
 * <p>All methods treat {@code null} operands as zero for arithmetic operations,
 * with the exception of {@link #divide} where a {@code null} or zero divisor
 * throws {@link ArithmeticException}.
 *
 * <p>Equality and comparison are always scale-insensitive (i.e. {@code 1.0}
 * and {@code 1.00} are considered equal).
 */
@UtilityClass
public class BigDecimalUtils
{
    /**
     * Returns {@code true} if both values are numerically equal, or both are {@code null}.
     *
     * <p>Unlike {@link BigDecimal#equals}, this method is scale-insensitive:
     * {@code 1.0} and {@code 1.00} are considered equal.
     *
     * @param a first value, may be {@code null}
     * @param b second value, may be {@code null}
     * @return {@code true} if both are {@code null} or {@code a.compareTo(b) == 0}
     */
    public boolean equals(final BigDecimal a, final BigDecimal b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        if (a == null || b == null)
        {
            return false;
        }
        return a.compareTo(b) == 0;
    }


    /**
     * Returns {@code true} if {@code value} is numerically equal to {@code longValue}.
     *
     * <p>Returns {@code false} if either argument is {@code null}.
     *
     * @param value     the {@link BigDecimal} operand, may be {@code null}
     * @param longValue the {@link Long} operand, may be {@code null}
     * @return {@code true} if both are non-null and numerically equal
     */
    public boolean equals(final BigDecimal value, final Long longValue)
    {
        if (value == null || longValue == null)
        {
            return false;
        }

        return value.compareTo(new BigDecimal(longValue)) == 0;
    }


    /**
     * Returns {@code true} if {@code value} is numerically equal to {@code biValue}.
     *
     * <p>Returns {@code false} if either argument is {@code null}.
     *
     * @param value   the {@link BigDecimal} operand, may be {@code null}
     * @param biValue the {@link BigInteger} operand, may be {@code null}
     * @return {@code true} if both are non-null and numerically equal
     */
    public boolean equals(final BigDecimal value, final BigInteger biValue)
    {
        if (value == null || biValue == null)
        {
            return false;
        }

        return value.compareTo(new BigDecimal(biValue)) == 0;
    }


    /**
     * Compares two {@link BigDecimal} values, treating {@code null} as less than
     * any non-null value (equivalent to {@code compare(a, b, true)}).
     *
     * @param a first value, may be {@code null}
     * @param b second value, may be {@code null}
     * @return negative if {@code a < b}, zero if equal, positive if {@code a > b}
     * @see #compare(BigDecimal, BigDecimal, boolean)
     */
    public int compare(final BigDecimal a, final BigDecimal b)
    {
        return compare(a, b, true);
    }


    /**
     * Compares two {@link BigDecimal} values with configurable {@code null} ordering.
     *
     * @param a          first value, may be {@code null}
     * @param b          second value, may be {@code null}
     * @param nullIsLess if {@code true}, {@code null} sorts before any non-null value;
     *                   if {@code false}, {@code null} sorts after any non-null value
     * @return negative if {@code a < b}, zero if equal, positive if {@code a > b}
     */
    public int compare(final BigDecimal a, final BigDecimal b, final boolean nullIsLess)
    {
        if (a == null)
        {
            return nullIsLess ? -1 : 1;
        }
        if (b == null)
        {
            return nullIsLess ? 1 : -1;
        }
        return a.compareTo(b);
    }


    /**
     * Compares a {@link BigDecimal} to a {@link Long}, treating {@code null} as less
     * than any non-null value.
     *
     * @param value     the {@link BigDecimal} operand, may be {@code null}
     * @param longValue the {@link Long} operand, may be {@code null}
     * @return negative if {@code value < longValue}, zero if equal, positive if greater
     * @see #compare(BigDecimal, BigDecimal, boolean)
     */
    public int compare(final BigDecimal value, final Long longValue)
    {
        return compare(value, longValue != null ? new BigDecimal(longValue) : null);
    }


    /**
     * Returns the sum of {@code a} and {@code b}, treating {@code null} as zero.
     *
     * <ul>
     *   <li>{@code null + null} → {@link BigDecimal#ZERO}</li>
     *   <li>{@code null + b} → {@code b}</li>
     *   <li>{@code a + null} → {@code a}</li>
     * </ul>
     *
     * @param a first addend, may be {@code null}
     * @param b second addend, may be {@code null}
     * @return the sum, never {@code null}
     */
    public BigDecimal add(final BigDecimal a, final BigDecimal b)
    {
        if (a == null && b == null)
        {
            return BigDecimal.ZERO;
        }
        if (a == null)
        {
            return b;
        }
        if (b == null)
        {
            return a;
        }
        return a.add(b);
    }


    /**
     * Returns the sum of {@code a} and {@code b}, treating {@code null} as zero.
     *
     * @param a first addend, may be {@code null}
     * @param b second addend as {@link Long}, may be {@code null}
     * @return the sum, never {@code null}
     * @see #add(BigDecimal, BigDecimal)
     */
    public BigDecimal add(final BigDecimal a, final Long b)
    {
        return add(a, b != null ? new BigDecimal(b) : null);
    }


    /**
     * Returns the difference {@code a - b}, treating {@code null} as zero.
     *
     * <ul>
     *   <li>{@code null - null} → {@link BigDecimal#ZERO}</li>
     *   <li>{@code null - b} → {@code -b}</li>
     *   <li>{@code a - null} → {@code a}</li>
     * </ul>
     *
     * @param a minuend, may be {@code null}
     * @param b subtrahend, may be {@code null}
     * @return the difference, never {@code null}
     */
    public BigDecimal subtract(final BigDecimal a, final BigDecimal b)
    {
        if (a == null && b == null)
        {
            return BigDecimal.ZERO;
        }
        if (a == null)
        {
            return b.negate();
        }
        if (b == null)
        {
            return a;
        }
        return a.subtract(b);
    }


    /**
     * Returns the difference {@code a - b}, treating {@code null} as zero.
     *
     * @param a minuend, may be {@code null}
     * @param b subtrahend as {@link Long}, may be {@code null}
     * @return the difference, never {@code null}
     * @see #subtract(BigDecimal, BigDecimal)
     */
    public BigDecimal subtract(final BigDecimal a, final Long b)
    {
        return subtract(a, b != null ? new BigDecimal(b) : null);
    }


    /**
     * Returns the product of {@code a} and {@code b}, treating {@code null} as zero.
     *
     * <p>If either operand is {@code null}, the result is {@link BigDecimal#ZERO}.
     *
     * @param a first factor, may be {@code null}
     * @param b second factor, may be {@code null}
     * @return the product, never {@code null}
     */
    public BigDecimal multiply(final BigDecimal a, final BigDecimal b)
    {
        if (a == null || b == null)
        {
            return BigDecimal.ZERO;
        }
        return a.multiply(b);
    }


    /**
     * Returns the product of {@code a} and {@code b}, treating {@code null} as zero.
     *
     * @param a first factor, may be {@code null}
     * @param b second factor as {@link Long}, may be {@code null}
     * @return the product, never {@code null}
     * @see #multiply(BigDecimal, BigDecimal)
     */
    public BigDecimal multiply(final BigDecimal a, final Long b)
    {
        return multiply(a, b != null ? new BigDecimal(b) : null);
    }


    /**
     * Divides {@code a} by {@code b} using {@link RoundingMode#HALF_UP}, preserving
     * the scale of the dividend.
     *
     * <p>If {@code a} is {@code null} it is treated as zero, returning
     * {@link BigDecimal#ZERO}.
     *
     * @param a dividend, may be {@code null}
     * @param b divisor, must not be {@code null} or zero
     * @return the quotient, never {@code null}
     * @throws ArithmeticException if {@code b} is {@code null} or zero
     */
    public BigDecimal divide(final BigDecimal a, final BigDecimal b)
    {
        if (b == null)
        {
            throw new ArithmeticException("Division by null");
        }
        if (a == null)
        {
            return BigDecimal.ZERO;
        }
        return a.divide(b, RoundingMode.HALF_UP);
    }


    /**
     * Divides {@code a} by {@code b} using {@link RoundingMode#HALF_UP}, preserving
     * the scale of the dividend.
     *
     * @param a dividend, may be {@code null}
     * @param b divisor as {@link Long}, must not be {@code null} or zero
     * @return the quotient, never {@code null}
     * @throws ArithmeticException if {@code b} is {@code null} or zero
     * @see #divide(BigDecimal, BigDecimal)
     */
    public BigDecimal divide(final BigDecimal a, final Long b)
    {
        return divide(a, b != null ? new BigDecimal(b) : null);
    }

}
