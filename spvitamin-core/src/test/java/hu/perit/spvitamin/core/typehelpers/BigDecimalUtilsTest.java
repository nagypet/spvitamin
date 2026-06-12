/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.core.typehelpers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalUtilsTest
{
    // -------------------------------------------------------------------------
    // equals(BigDecimal, BigDecimal)
    // -------------------------------------------------------------------------


    @Test
    void equals_bothNull_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals((BigDecimal) null, (BigDecimal) null)).isTrue();
    }


    @Test
    void equals_firstNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(null, BigDecimal.ONE)).isFalse();
    }


    @Test
    void equals_secondNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(BigDecimal.ONE, (BigDecimal) null)).isFalse();
    }


    @Test
    void equals_sameValue_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.5"), new BigDecimal("1.5"))).isTrue();
    }


    @Test
    void equals_sameNumericValueDifferentScale_returnsTrue()
    {
        // 1.0 and 1.00 are numerically equal — equals() should be scale-insensitive
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.0"), new BigDecimal("1.00"))).isTrue();
    }


    @Test
    void equals_differentValues_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.0"), new BigDecimal("2.0"))).isFalse();
    }


    @Test
    void equals_zero_variations_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(BigDecimal.ZERO, new BigDecimal("0.00"))).isTrue();
    }

    // -------------------------------------------------------------------------
    // equals(BigDecimal, Long)
    // -------------------------------------------------------------------------


    @Test
    void equals_bigDecimalAndLong_bothNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals((BigDecimal) null, (Long) null)).isFalse();
    }


    @Test
    void equals_bigDecimalAndLong_bigDecimalNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals((BigDecimal) null, 1L)).isFalse();
    }


    @Test
    void equals_bigDecimalAndLong_longNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(BigDecimal.ONE, (Long) null)).isFalse();
    }


    @Test
    void equals_bigDecimalAndLong_equalValues_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("1"), 1L)).isTrue();
    }


    @Test
    void equals_bigDecimalAndLong_decimalWithZeroFraction_returnsTrue()
    {
        // 1.0 numerically equals 1L
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.0"), 1L)).isTrue();
    }


    @Test
    void equals_bigDecimalAndLong_nonIntegerBigDecimal_returnsFalse()
    {
        // 1.5 is NOT equal to 1L — must not truncate
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.5"), 1L)).isFalse();
    }


    @Test
    void equals_bigDecimalAndLong_differentValues_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("2"), 1L)).isFalse();
    }


    @Test
    void equals_bigDecimalAndLong_negativeEqualValues_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("-5"), -5L)).isTrue();
    }


    @Test
    void equals_bigDecimalAndLong_longMaxValue_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal(Long.MAX_VALUE), Long.MAX_VALUE)).isTrue();
    }

    // -------------------------------------------------------------------------
    // equals(BigDecimal, BigInteger)
    // -------------------------------------------------------------------------


    @Test
    void equals_bigDecimalAndBigInteger_bothNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals((BigDecimal) null, (BigInteger) null)).isFalse();
    }


    @Test
    void equals_bigDecimalAndBigInteger_bigDecimalNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals((BigDecimal) null, BigInteger.ONE)).isFalse();
    }


    @Test
    void equals_bigDecimalAndBigInteger_bigIntegerNull_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(BigDecimal.ONE, (BigInteger) null)).isFalse();
    }


    @Test
    void equals_bigDecimalAndBigInteger_equalValues_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("1"), BigInteger.ONE)).isTrue();
    }


    @Test
    void equals_bigDecimalAndBigInteger_decimalWithZeroFraction_returnsTrue()
    {
        // 1.0 is numerically equal to BigInteger 1 — must not fail on scale
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.0"), BigInteger.ONE)).isTrue();
    }


    @Test
    void equals_bigDecimalAndBigInteger_nonIntegerBigDecimal_returnsFalse()
    {
        // 1.5 is NOT equal to BigInteger 1
        assertThat(BigDecimalUtils.equals(new BigDecimal("1.5"), BigInteger.ONE)).isFalse();
    }


    @Test
    void equals_bigDecimalAndBigInteger_differentValues_returnsFalse()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("2"), BigInteger.ONE)).isFalse();
    }


    @Test
    void equals_bigDecimalAndBigInteger_negativeEqualValues_returnsTrue()
    {
        assertThat(BigDecimalUtils.equals(new BigDecimal("-3"), BigInteger.valueOf(-3))).isTrue();
    }

    // -------------------------------------------------------------------------
    // compare(BigDecimal, BigDecimal) — default nullIsLess = true
    // -------------------------------------------------------------------------


    @Test
    void compare_lessThan_returnsNegative()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("1"), new BigDecimal("2"))).isNegative();
    }


    @Test
    void compare_greaterThan_returnsPositive()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("2"), new BigDecimal("1"))).isPositive();
    }


    @Test
    void compare_equal_returnsZero()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("1"), new BigDecimal("1"))).isZero();
    }


    @Test
    void compare_equalDifferentScale_returnsZero()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("1.0"), new BigDecimal("1.00"))).isZero();
    }


    @Test
    void compare_firstNullDefaultNullIsLess_returnsNegative()
    {
        assertThat(BigDecimalUtils.compare(null, BigDecimal.ONE)).isNegative();
    }


    @Test
    void compare_secondNullDefaultNullIsLess_returnsPositive()
    {
        assertThat(BigDecimalUtils.compare(BigDecimal.ONE, (BigDecimal) null)).isPositive();
    }

    // -------------------------------------------------------------------------
    // compare(BigDecimal, BigDecimal, boolean nullIsLess)
    // -------------------------------------------------------------------------


    @Test
    void compare_firstNullNullIsTrue_returnsNegative()
    {
        assertThat(BigDecimalUtils.compare(null, BigDecimal.ONE, true)).isNegative();
    }


    @Test
    void compare_firstNullNullIsFalse_returnsPositive()
    {
        assertThat(BigDecimalUtils.compare(null, BigDecimal.ONE, false)).isPositive();
    }


    @Test
    void compare_secondNullNullIsTrue_returnsPositive()
    {
        assertThat(BigDecimalUtils.compare(BigDecimal.ONE, null, true)).isPositive();
    }


    @Test
    void compare_secondNullNullIsFalse_returnsNegative()
    {
        assertThat(BigDecimalUtils.compare(BigDecimal.ONE, null, false)).isNegative();
    }


    @Test
    void compare_bothNonNull_ignoresNullIsLessFlag()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("3"), new BigDecimal("3"), true)).isZero();
        assertThat(BigDecimalUtils.compare(new BigDecimal("3"), new BigDecimal("3"), false)).isZero();
    }

    // -------------------------------------------------------------------------
    // compare(BigDecimal, Long)
    // -------------------------------------------------------------------------


    @Test
    void compare_bigDecimalAndLong_lessThan_returnsNegative()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("1"), 2L)).isNegative();
    }


    @Test
    void compare_bigDecimalAndLong_greaterThan_returnsPositive()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("2"), 1L)).isPositive();
    }


    @Test
    void compare_bigDecimalAndLong_equal_returnsZero()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("1"), 1L)).isZero();
    }


    @Test
    void compare_bigDecimalAndLong_decimalValueEqualToLong_returnsZero()
    {
        assertThat(BigDecimalUtils.compare(new BigDecimal("1.0"), 1L)).isZero();
    }


    @Test
    void compare_bigDecimalAndLong_bigDecimalNull_doesNotThrow()
    {
        // null BigDecimal should be treated as null (less than any value)
        assertThat(BigDecimalUtils.compare(null, 1L)).isNegative();
    }


    @Test
    void compare_bigDecimalAndLong_longNull_doesNotThrow()
    {
        // null Long should be handled gracefully
        assertThat(BigDecimalUtils.compare(BigDecimal.ONE, (Long) null)).isPositive();
    }


    @Test
    void compare_bigDecimalAndLong_checkIfPositive()
    {
        assertThat(BigDecimalUtils.compare(null, 0L) > 0).isFalse();
        assertThat(BigDecimalUtils.compare(BigDecimal.ZERO, 0L) > 0).isFalse();
        assertThat(BigDecimalUtils.compare(new BigDecimal(-1), 0L) > 0).isFalse();
        assertThat(BigDecimalUtils.compare(BigDecimal.ONE, 0L) > 0).isTrue();
    }

    // -------------------------------------------------------------------------
    // add(BigDecimal, BigDecimal)
    // -------------------------------------------------------------------------


    @Test
    void add_bothNull_returnsZero()
    {
        assertThat(BigDecimalUtils.add((BigDecimal) null, (BigDecimal) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void add_firstNull_returnsSecond()
    {
        assertThat(BigDecimalUtils.add(null, new BigDecimal("3.5"))).isEqualByComparingTo(new BigDecimal("3.5"));
    }


    @Test
    void add_secondNull_returnsFirst()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("3.5"), (BigDecimal) null)).isEqualByComparingTo(new BigDecimal("3.5"));
    }


    @Test
    void add_bothNonNull_returnsSum()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("1.5"), new BigDecimal("2.5"))).isEqualByComparingTo(new BigDecimal("4.0"));
    }


    @Test
    void add_negativeAndPositive_returnsCorrectSum()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("-3"), new BigDecimal("5"))).isEqualByComparingTo(new BigDecimal("2"));
    }


    @Test
    void add_bothZero_returnsZero()
    {
        assertThat(BigDecimalUtils.add(BigDecimal.ZERO, BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void add_negativeValues_returnsNegativeSum()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("-1.5"), new BigDecimal("-2.5"))).isEqualByComparingTo(new BigDecimal("-4.0"));
    }

    // -------------------------------------------------------------------------
    // add(BigDecimal, Long)
    // -------------------------------------------------------------------------


    @Test
    void add_bigDecimalAndLong_bothNull_returnsZero()
    {
        assertThat(BigDecimalUtils.add((BigDecimal) null, (Long) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void add_bigDecimalAndLong_bigDecimalNull_returnsLong()
    {
        assertThat(BigDecimalUtils.add(null, 3L)).isEqualByComparingTo(new BigDecimal("3"));
    }


    @Test
    void add_bigDecimalAndLong_longNull_returnsBigDecimal()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("3.5"), (Long) null)).isEqualByComparingTo(new BigDecimal("3.5"));
    }


    @Test
    void add_bigDecimalAndLong_bothNonNull_returnsSum()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("1.5"), 2L)).isEqualByComparingTo(new BigDecimal("3.5"));
    }


    @Test
    void add_bigDecimalAndLong_negativeValues_returnsCorrectSum()
    {
        assertThat(BigDecimalUtils.add(new BigDecimal("-3.5"), -2L)).isEqualByComparingTo(new BigDecimal("-5.5"));
    }

    // -------------------------------------------------------------------------
    // subtract(BigDecimal, BigDecimal)
    // -------------------------------------------------------------------------


    @Test
    void subtract_bothNull_returnsZero()
    {
        assertThat(BigDecimalUtils.subtract((BigDecimal) null, (BigDecimal) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void subtract_firstNull_returnsNegatedSecond()
    {
        // null - b = 0 - b = -b
        assertThat(BigDecimalUtils.subtract(null, new BigDecimal("3.5"))).isEqualByComparingTo(new BigDecimal("-3.5"));
    }


    @Test
    void subtract_secondNull_returnsFirst()
    {
        // a - null = a - 0 = a
        assertThat(BigDecimalUtils.subtract(new BigDecimal("3.5"), (BigDecimal) null)).isEqualByComparingTo(new BigDecimal("3.5"));
    }


    @Test
    void subtract_bothNonNull_returnsDifference()
    {
        assertThat(BigDecimalUtils.subtract(new BigDecimal("5.0"), new BigDecimal("2.5"))).isEqualByComparingTo(new BigDecimal("2.5"));
    }


    @Test
    void subtract_resultNegative_returnsNegative()
    {
        assertThat(BigDecimalUtils.subtract(new BigDecimal("1"), new BigDecimal("3"))).isEqualByComparingTo(new BigDecimal("-2"));
    }


    @Test
    void subtract_sameValues_returnsZero()
    {
        assertThat(BigDecimalUtils.subtract(new BigDecimal("5.5"), new BigDecimal("5.5"))).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // -------------------------------------------------------------------------
    // subtract(BigDecimal, Long)
    // -------------------------------------------------------------------------


    @Test
    void subtract_bigDecimalAndLong_bothNull_returnsZero()
    {
        assertThat(BigDecimalUtils.subtract((BigDecimal) null, (Long) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void subtract_bigDecimalAndLong_bigDecimalNull_returnsNegatedLong()
    {
        assertThat(BigDecimalUtils.subtract(null, 3L)).isEqualByComparingTo(new BigDecimal("-3"));
    }


    @Test
    void subtract_bigDecimalAndLong_longNull_returnsBigDecimal()
    {
        assertThat(BigDecimalUtils.subtract(new BigDecimal("5.5"), (Long) null)).isEqualByComparingTo(new BigDecimal("5.5"));
    }


    @Test
    void subtract_bigDecimalAndLong_bothNonNull_returnsDifference()
    {
        assertThat(BigDecimalUtils.subtract(new BigDecimal("7.5"), 3L)).isEqualByComparingTo(new BigDecimal("4.5"));
    }


    @Test
    void subtract_bigDecimalAndLong_negativeResult_returnsNegative()
    {
        assertThat(BigDecimalUtils.subtract(new BigDecimal("2.0"), 5L)).isEqualByComparingTo(new BigDecimal("-3.0"));
    }

    // -------------------------------------------------------------------------
    // multiply(BigDecimal, BigDecimal)
    // -------------------------------------------------------------------------


    @Test
    void multiply_firstNull_returnsZero()
    {
        assertThat(BigDecimalUtils.multiply((BigDecimal) null, new BigDecimal("3"))).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void multiply_secondNull_returnsZero()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("3"), (BigDecimal) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void multiply_bothNull_returnsZero()
    {
        assertThat(BigDecimalUtils.multiply((BigDecimal) null, (BigDecimal) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void multiply_bothNonNull_returnsProduct()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("2.5"), new BigDecimal("4"))).isEqualByComparingTo(new BigDecimal("10"));
    }


    @Test
    void multiply_byZero_returnsZero()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("99.99"), BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void multiply_negativeValues_returnsPositiveProduct()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("-3"), new BigDecimal("-4"))).isEqualByComparingTo(new BigDecimal("12"));
    }


    @Test
    void multiply_negativeAndPositive_returnsNegativeProduct()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("-3"), new BigDecimal("4"))).isEqualByComparingTo(new BigDecimal("-12"));
    }

    // -------------------------------------------------------------------------
    // multiply(BigDecimal, Long)
    // -------------------------------------------------------------------------


    @Test
    void multiply_bigDecimalAndLong_firstNull_returnsZero()
    {
        assertThat(BigDecimalUtils.multiply(null, 3L)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void multiply_bigDecimalAndLong_longNull_returnsZero()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("3"), (Long) null)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void multiply_bigDecimalAndLong_bothNonNull_returnsProduct()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("2.5"), 4L)).isEqualByComparingTo(new BigDecimal("10"));
    }


    @Test
    void multiply_bigDecimalAndLong_negativeValues_returnsPositiveProduct()
    {
        assertThat(BigDecimalUtils.multiply(new BigDecimal("-3"), -4L)).isEqualByComparingTo(new BigDecimal("12"));
    }

    // -------------------------------------------------------------------------
    // divide(BigDecimal, BigDecimal)
    // -------------------------------------------------------------------------


    @Test
    void divide_numeratorNull_returnsZero()
    {
        assertThat(BigDecimalUtils.divide((BigDecimal) null, new BigDecimal("3"))).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void divide_denominatorNull_throwsArithmeticException()
    {
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
            () -> BigDecimalUtils.divide(new BigDecimal("3"), (BigDecimal) null));
    }


    @Test
    void divide_bothNonNull_returnsQuotient()
    {
        // 10.00 / 4 = 2.50 (scale preserved from dividend)
        assertThat(BigDecimalUtils.divide(new BigDecimal("10.00"), new BigDecimal("4"))).isEqualByComparingTo(new BigDecimal("2.5"));
    }


    @Test
    void divide_nonTerminatingResult_roundsHalfUp()
    {
        // 10.00 / 3 = 3.33... rounded HALF_UP at scale 2 → 3.33
        assertThat(BigDecimalUtils.divide(new BigDecimal("10.00"), new BigDecimal("3"))).isEqualByComparingTo(new BigDecimal("3.33"));
    }


    @Test
    void divide_nonTerminatingResultRoundUp_roundsHalfUp()
    {
        // 2.000 / 3 = 0.666... rounded HALF_UP at scale 3 → 0.667
        assertThat(BigDecimalUtils.divide(new BigDecimal("2.000"), new BigDecimal("3"))).isEqualByComparingTo(new BigDecimal("0.667"));
    }


    @Test
    void divide_byZero_throwsArithmeticException()
    {
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
            () -> BigDecimalUtils.divide(new BigDecimal("10"), BigDecimal.ZERO));
    }


    @Test
    void divide_negativeNumerator_returnsNegativeQuotient()
    {
        assertThat(BigDecimalUtils.divide(new BigDecimal("-10.00"), new BigDecimal("4"))).isEqualByComparingTo(new BigDecimal("-2.5"));
    }


    @Test
    void divide_bothNegative_returnsPositiveQuotient()
    {
        assertThat(BigDecimalUtils.divide(new BigDecimal("-10.00"), new BigDecimal("-4"))).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    // -------------------------------------------------------------------------
    // divide(BigDecimal, Long)
    // -------------------------------------------------------------------------


    @Test
    void divide_bigDecimalAndLong_numeratorNull_returnsZero()
    {
        assertThat(BigDecimalUtils.divide(null, 3L)).isEqualByComparingTo(BigDecimal.ZERO);
    }


    @Test
    void divide_bigDecimalAndLong_denominatorNull_throwsArithmeticException()
    {
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
            () -> BigDecimalUtils.divide(new BigDecimal("10"), (Long) null));
    }


    @Test
    void divide_bigDecimalAndLong_bothNonNull_returnsQuotient()
    {
        assertThat(BigDecimalUtils.divide(new BigDecimal("10.00"), 4L)).isEqualByComparingTo(new BigDecimal("2.5"));
    }


    @Test
    void divide_bigDecimalAndLong_byZero_throwsArithmeticException()
    {
        org.junit.jupiter.api.Assertions.assertThrows(ArithmeticException.class,
            () -> BigDecimalUtils.divide(new BigDecimal("10"), 0L));
    }


    @Test
    void divide_bigDecimalAndLong_negativeValues_returnsCorrectQuotient()
    {
        assertThat(BigDecimalUtils.divide(new BigDecimal("-10.00"), -4L)).isEqualByComparingTo(new BigDecimal("2.5"));
    }
}
