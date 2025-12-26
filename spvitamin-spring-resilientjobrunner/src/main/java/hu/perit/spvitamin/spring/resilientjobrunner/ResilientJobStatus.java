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

package hu.perit.spvitamin.spring.resilientjobrunner;

import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Generated // To disable counting in unit test coverage
public enum ResilientJobStatus
{
    CREATED(0),
    IN_PROGRESS(10),
    DONE(20),
    ERROR(30);

    private final long value;

    public static Optional<ResilientJobStatus> fromValue(Long input)
    {
        return Arrays.stream(ResilientJobStatus.values()).filter(i -> i.value == input).findFirst();
    }
}
