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

package hu.perit.spvitamin.core.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NumberConverterTest
{

    @Test
    void test()
    {
        assertThat(NumberConverter.fromText("1,973.50")).isEqualTo(new BigDecimal("1973.50"));
        assertThat(NumberConverter.fromText("1973.50")).isEqualTo(new BigDecimal("1973.50"));
        assertThat(NumberConverter.fromText("1973,50")).isEqualTo(new BigDecimal("1973.50"));
        assertThat(NumberConverter.fromText("1.973,50")).isEqualTo(new BigDecimal("1973.50"));
        assertThat(NumberConverter.fromText("1 973,50")).isEqualTo(new BigDecimal("1973.50"));
    }
}
