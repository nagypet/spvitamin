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

package hu.perit.spvitamin.core.took;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A utility class for measuring and logging method execution time.
 * 
 * <p>This class implements AutoCloseable to enable usage with try-with-resources blocks,
 * automatically logging the execution time when the block completes. It captures the
 * calling method name and provides various constructors to customize the behavior.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Automatic method name detection from the call stack</li>
 *   <li>Optional context information in logs</li>
 *   <li>Configurable logging behavior</li>
 *   <li>Detailed timing information including start and end timestamps</li>
 *   <li>Support for explicit method references</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>
 * try (Took took = new Took("context info")) {
 *     // Code to measure
 * } // Execution time is automatically logged when exiting the block
 * </pre>
 * 
 * @author Peter Nagy
 */

@Slf4j
@Getter
public class Took implements AutoCloseable
{

    protected String methodName;
    private final long startTimeMillis;
    private final boolean logAtClose;

    public Took()
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName("");
    }

    public Took(boolean logAtClose)
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = logAtClose;
        this.methodName = getCallingMethodName("");
    }

    public Took(String context)
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName(context);
    }

    public Took(Method method)
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName(method, "");
    }

    public Took(Method method, boolean logAtClose)
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = logAtClose;
        this.methodName = getCallingMethodName(method, "");
    }

    public Took(Method method, String context)
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName(method, context);
    }

    public Took(boolean logAtClose, String context)
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = logAtClose;
        this.methodName = getCallingMethodName(context);
    }


    protected String getCallingMethodName(String context)
    {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        return String.format("%s.%s(%s)", getAbbreviatedClassName(stackTraceElement.getClassName()), stackTraceElement.getMethodName(),
            context);
    }


    protected String getCallingMethodName(Method method, String context)
    {
        return String.format("%s.%s(%s)", getAbbreviatedClassName(method.getDeclaringClass().getName()), method.getName(), context);
    }


    public long getDuration()
    {
        return System.currentTimeMillis() - this.startTimeMillis;
    }


    protected static String getAbbreviatedClassName(String canonicalName)
    {
        String[] packages = canonicalName.split("\\.");
        if (packages == null || packages.length <= 1)
        {
            return canonicalName;
        }

        int lastindex = packages.length - 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lastindex - 1; i++)
        {
            sb.append(packages[i].substring(0, 1));
            sb.append(".");
        }

        sb.append(packages[lastindex]);
        return sb.toString();
    }


    @Override
    public void close()
    {
        if (this.logAtClose)
        {
            long endTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            log.debug(String.format("%s took %d ms. Start time: %s, end time: %s", this.methodName, endTimeMillis - this.startTimeMillis,
                sdf.format(new Date(this.startTimeMillis)), sdf.format(new Date(endTimeMillis))));
        }
    }
}
