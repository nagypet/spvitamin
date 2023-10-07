/*
 * Copyright 2020-2023 the original author or authors.
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

package hu.perit.spvitamin.spring.json;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class AcceptedDateFormats
{
    private static final List<String> FORMATS = new ArrayList<>();

    static
    {
        FORMATS.add("yyyy-MM-dd HH:mm:ss.SSS");
        FORMATS.add("yyyy-MM-dd HH:mm:ss");
        FORMATS.add("yyyy-MM-dd HH:mm");
        FORMATS.add("yyyy-MM-dd");
        FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
        FORMATS.add("yyyy-MM-dd'T'HH:mm:ss");
        FORMATS.add("yyyy-MM-dd'T'HH:mm");
        FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        FORMATS.add("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    public static List<String> getAcceptedDateFormats()
    {
        return Collections.unmodifiableList(FORMATS);
    }
}