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

package hu.perit.spvitamin.spring.restmethodlogger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Please start the Java VM with -Djava.net.preferIPv4Stack=true
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoggedRestMethod
{
    int eventId();
    String subsystem();
    boolean muted() default false;

    /**
     * These arguments will be put into the ThreadContext for logging
     */
    String[] ctx() default {};

    /**
     * This argument will be provided as externalTraceId in the LogEvent
     */
    String externalTraceId() default "";

    /**
     * If not the logged-in user should be used for user
     */
    String user() default "";
}
