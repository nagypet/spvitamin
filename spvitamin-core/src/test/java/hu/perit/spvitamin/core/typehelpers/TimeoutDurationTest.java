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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimeoutDurationTest
{

    @Test
    void test()
    {
        TimeoutDuration duration = TimeoutDurationStyle.detectAndParse("24h");
        assertThat(duration.getDuration().toHours()).isEqualTo(24);
        assertThat(duration.getUnit()).isEqualTo(ChronoUnit.HOURS);
    }


    @Test
    @DisplayName("getRoundedDuration should return correct duration when activation just started")
    void getRoundedDuration_justStarted()
    {
        // Arrange
        TimeoutDuration timeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS);

        // Act
        Duration remainingDuration = timeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration).isEqualTo(timeout.getDuration());
    }


    @Test
    @DisplayName("getRoundedDuration should return correct duration when some time has passed")
    void getRoundedDuration_someTimePassed()
    {
        // Arrange
        TimeoutDuration reminingTimeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS)
                .elapsed(Duration.ofHours(12));

        // Act
        Duration remainingDuration = reminingTimeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toHours()).isEqualTo(12);
    }


    @Test
    @DisplayName("getRoundedDuration should return correct duration when almost expired")
    void getRoundedDuration_almostExpired()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS)
                .elapsed(Duration.ofHours(23).plusMinutes(59));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toMinutes()).isEqualTo(1); // Should be 1 minute when rounded to minutes
    }


    @Test
    @DisplayName("getRoundedDuration should handle expired timeout")
    void getRoundedDuration_expired()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS)
                .elapsed(Duration.ofHours(25));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.isNegative()).isFalse();
        assertThat(remainingDuration.toHours()).isZero();
    }


    @Test
    @DisplayName("getRoundedDuration should handle future activation timestamp")
    void getRoundedDuration_futureActivation()
    {
        // Arrange
        TimeoutDuration reminingTimeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS)
                .elapsed(Duration.ofMillis(1).minusHours(3));

        // Act
        Duration remainingDuration = reminingTimeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toHours()).isGreaterThan(24);
    }


    @Test
    @DisplayName("getRoundedDuration should round to hours for day-based timeout")
    void getRoundedDuration_roundToDays()
    {
        // Arrange
        TimeoutDuration timeout = TimeoutDuration.of(Duration.ofDays(7), ChronoUnit.DAYS)
                .elapsed(Duration.ofDays(3).plusHours(12));

        // Act
        Duration remainingDuration = timeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toDays()).isEqualTo(3);
        assertThat(remainingDuration.toHours() % 24).isEqualTo(12); // Should be rounded to hours, not days
    }


    @Test
    @DisplayName("getRoundedDuration should round to minutes for hour-based timeout")
    void getRoundedDuration_roundToHours()
    {
        // Arrange
        TimeoutDuration timeout = TimeoutDuration.of(Duration.ofHours(48), ChronoUnit.HOURS)
                .elapsed(Duration.ofHours(25).plusMinutes(30));

        // Act
        Duration remainingDuration = timeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toHours()).isEqualTo(22);
        assertThat(remainingDuration.toMinutes() % 60).isEqualTo(30); // Should be rounded to minutes, not hours
    }


    @Test
    @DisplayName("getRoundedDuration should round to seconds for minute-based timeout")
    void getRoundedDuration_roundToMinutes()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofMinutes(120), ChronoUnit.MINUTES)
                .elapsed(Duration.ofMinutes(65).plusSeconds(30));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toMinutes()).isEqualTo(54);
        assertThat(remainingDuration.getSeconds() % 60).isEqualTo(30); // Should be rounded to seconds, not minutes
    }


    @Test
    @DisplayName("getRoundedDuration with roundToSmallerUnitCount=0 should round to the same unit")
    void getRoundedDuration_roundToSameUnit()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS)
                .elapsed(Duration.ofHours(12).plusMinutes(30));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration(0);

        // Assert
        assertThat(remainingDuration.toHours()).isEqualTo(11);
        assertThat(remainingDuration.toMinutes() % 60).isZero(); // Should be rounded to hours, not minutes
    }


    @Test
    @DisplayName("getRoundedDuration with roundToSmallerUnitCount=1 should round to one unit smaller")
    void getRoundedDuration_roundToOneUnitSmaller()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofHours(24), ChronoUnit.HOURS)
                .elapsed(Duration.ofHours(12).plusMinutes(30));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration();

        // Assert
        assertThat(remainingDuration.toHours()).isEqualTo(11);
        assertThat(remainingDuration.toMinutes() % 60).isEqualTo(30); // Should be rounded to minutes
    }


    @Test
    @DisplayName("getRoundedDuration with roundToSmallerUnitCount=2 should round to two units smaller")
    void getRoundedDuration_roundToTwoUnitsSmaller()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofDays(7), ChronoUnit.DAYS)
                .elapsed(Duration.ofDays(3).plusHours(12).plusMinutes(30));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration(2);

        // Assert
        assertThat(remainingDuration.toDays()).isEqualTo(3);
        assertThat(remainingDuration.toHours() % 24).isEqualTo(11);
        assertThat(remainingDuration.toMinutes() % 60).isEqualTo(30); // Should be rounded to minutes
    }


    @Test
    @DisplayName("getRoundedDuration with roundToSmallerUnitCount larger than available units should round to smallest available unit")
    void getRoundedDuration_roundToSmallestAvailableUnit()
    {
        // Arrange
        TimeoutDuration remainingTimeout = TimeoutDuration.of(Duration.ofSeconds(120), ChronoUnit.SECONDS)
                .elapsed(Duration.ofSeconds(60).plusMillis(500));

        // Act
        Duration remainingDuration = remainingTimeout.getRoundedDuration(5); // More than available units

        // Assert
        assertThat(remainingDuration.getSeconds()).isEqualTo(59);
        assertThat(remainingDuration.toMillis() % 1000).isEqualTo(500); // Should be rounded to millis (smallest available)
    }
}
