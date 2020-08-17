/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.environment;

import hu.perit.spvitamin.core.exception.UnexpectedConditionException;
import org.springframework.core.env.Environment;

public class SpringEnvironment {

    private static SpringEnvironment instance = new SpringEnvironment();
    private Environment environment;

    public static SpringEnvironment getInstance() {
        return instance;
    }

    private SpringEnvironment() {
    }

    public Environment get() {
        if (this.environment == null) {
            throw new UnexpectedConditionException("There is no injected Environment!");
        }
        return environment;
    }

    public void set(Environment environment) {
        this.environment = environment;
    }
}
