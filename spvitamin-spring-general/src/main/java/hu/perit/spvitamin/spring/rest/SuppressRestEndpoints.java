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
 * Marks a controller class whose endpoint methods should NOT be registered or exported.
 * Methods declared in an annotated class are suppressed — they will not appear in Spring MVC
 * or in the OpenAPI/Swagger documentation — even if the class implements a REST API interface.
 * <p>
 * Apply this annotation to any abstract base controller from which REST endpoints should
 * NOT be inherited. Concrete subclasses that override a method will still expose it normally.
 *
 * @see SuppressRestEndpointsConfig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SuppressRestEndpoints
{
}
