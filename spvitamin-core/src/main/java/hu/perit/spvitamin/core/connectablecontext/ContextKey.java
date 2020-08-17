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

package hu.perit.spvitamin.core.connectablecontext;

/**
 * @author Peter Nagy
 */


public abstract class ContextKey
{
    public abstract boolean equals(Object o);
    public abstract int hashCode();
    public abstract String toString();

    // This shall return true, if the key has become invalid. If the context key is related to a thread,
    // the thread may die for several reasons. For a dead thread isInvalid() shall return true.
    public boolean isInvalid() {return false;}
}
