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

package hu.perit.spvitamin.spring.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Peter Nagy
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String DEFAULT_JACKSON_TIMESTAMPFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DEFAULT_JACKSON_ZONEDTIMESTAMPFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DEFAULT_JACKSON_DATEFORMAT = "yyyy-MM-dd";

    public static final String SUBSYSTEM_NAME = "admin-gui";
}
