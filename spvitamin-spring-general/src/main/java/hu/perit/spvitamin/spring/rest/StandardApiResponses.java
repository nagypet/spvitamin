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

package hu.perit.spvitamin.spring.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a REST endpoint method with a set of standard OpenAPI response codes.
 * <p>
 * Processed at runtime by {@link StandardApiResponsesCustomizer}, which adds the
 * appropriate Swagger/OpenAPI response descriptions to the generated documentation.
 * <p>
 * Supported codes: 200, 201, 400, 401, 403, 404, 409, 500.
 *
 * <pre>{@code
 * @StandardApiResponses(codes = {200, 400, 404, 500})
 * @GetMapping("/example")
 * ResponseEntity<Foo> getExample();
 * }</pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StandardApiResponses
{
    int[] codes() default {200, 500};
}
