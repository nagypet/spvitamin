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

package hu.perit.spvitamin.core.invoker;

import hu.perit.spvitamin.core.exception.ServerException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FixedMethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Invokes target by method name.
 *
 * @author Peter Nagy
 */

public interface Invoker
{
    default Object invoke(Object target, String name, Object... args) throws InvocationTargetException
    {
        Objects.requireNonNull(target, "Invocation target is 'null'!");

        try
        {
            Class<?>[] argTypes = ClassUtils.toClass(args);
            Object retVal = FixedMethodUtils.invokeMethod(target, true, name, args);
            Method method = FixedMethodUtils.getMatchingMethod(target.getClass(), name, argTypes);
            if (method != null)
            {
                Class<?> returnType = method.getReturnType();
                if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive())
                {
                    throw new RuntimeException("Null return value does not match primitive return type for: " + method);
                }
            }

            return retVal;
        }
        catch (NoSuchMethodException | IllegalAccessException e)
        {
            return ServerException.throwFrom(e);
        }
    }

    static String getMyMethodName()
    {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        return stackTraceElement.getMethodName();
    }
}
