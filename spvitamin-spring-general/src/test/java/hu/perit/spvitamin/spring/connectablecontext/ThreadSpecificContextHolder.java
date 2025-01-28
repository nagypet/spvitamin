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

package hu.perit.spvitamin.spring.connectablecontext;

import hu.perit.spvitamin.core.connectablecontext.ConnectableContextCollection;
import hu.perit.spvitamin.core.connectablecontext.SimpleConnectableContextHolder;

/**
 * @author Peter Nagy
 */

public class ThreadSpecificContextHolder extends SimpleConnectableContextHolder<ThreadSpecificContext>
{
    private static ThreadSpecificContextHolder instance = new ThreadSpecificContextHolder();

    public static ThreadSpecificContextHolder getInstance()
    {
        return instance;
    }

    private ThreadSpecificContextHolder()
    {
    }

    private ConnectableContextCollection<ThreadSpecificContext> contextCollection = new ConnectableContextCollection<>(ThreadSpecificContext::new);

    @Override
    protected ConnectableContextCollection getContextCollection()
    {
        return this.contextCollection;
    }
}
