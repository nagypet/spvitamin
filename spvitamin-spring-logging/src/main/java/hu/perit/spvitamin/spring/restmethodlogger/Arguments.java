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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class Arguments
{
    @Setter(AccessLevel.NONE)
    private final Map<String, Object> argumentMap = new LinkedHashMap<>();

    public static Arguments create(List<String> argNames, Object... args)
    {
        Arguments retval = new Arguments();
        if (args == null || args.length == 0)
        {
            return retval;
        }

        for (int i = 0; i < argNames.size(); i++)
        {
            if (i < args.length)
            {
                retval.argumentMap.put(argNames.get(i), args[i]);
            }
            else
            {
                retval.argumentMap.put(argNames.get(i), null);
            }
        }
        return retval;
    }

    public Object get(String argName)
    {
        return argumentMap.get(argName);
    }

    public String getString(String argName)
    {
        Object object = argumentMap.get(argName);
        if (object != null)
        {
            return object.toString();
        }
        return null;
    }

    public boolean isEmpty()
    {
        return this.argumentMap.isEmpty();
    }
}
