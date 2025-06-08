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

package hu.perit.spvitamin.core;

import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A utility class for enhanced stack trace handling and exception formatting.
 * 
 * <p>This class provides methods to format and display stack traces in a more readable
 * and useful way than the standard Java stack trace output. It can filter stack traces
 * to show only relevant parts, track which stack trace elements have already been printed,
 * and format exceptions with their causes in a compact form.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Configurable "own package" detection to highlight application-specific code</li>
 *   <li>Compact exception formatting with cause chains</li>
 *   <li>Partial stack trace extraction to avoid redundancy</li>
 *   <li>Current thread stack trace formatting</li>
 * </ul>
 * 
 * @author Peter Nagy
 */

public class StackTracer
{

    private static final Set<String> ownPackages = new HashSet<>();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    static
    {
        addOwnPackage("hu.perit");
    }

    public static void addOwnPackage(String packageName)
    {
        lock.writeLock().lock();
        try
        {
            ownPackages.add(packageName);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    private StackTracer()
    {
        throw new IllegalStateException("Utility class");
    }

    public static String toString(Throwable ex)
    {
        Set<StackTraceElement> alreadyPrinted = new HashSet<>();
        return toStringInternal(ex, alreadyPrinted);
    }


    public static String toStringInternal(Throwable ex, Set<StackTraceElement> alreadyPrinted)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(removeLineSeparators(ex.toString()));

        appendWithLineSeparator(sb, getPartialStackTrace(alreadyPrinted, ex, false));

        if (ex.getCause() != null)
        {
            appendWithLineSeparator(sb, "    caused by: " + toStringInternal(ex.getCause(), alreadyPrinted));
        }
        return sb.toString();
    }


    public static String toStringCompact(Throwable ex)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ExceptionWrapper.of(ex).toStringWithCauses());
        sb.append(currentThreadToString());

        return sb.toString();
    }


    private static void appendWithLineSeparator(StringBuilder sb, String message)
    {
        String lastChar = "";
        if (sb.length() > 0)
        {
            lastChar = sb.substring(sb.length() - 1);
        }
        if (!lastChar.equals("\n") && StringUtils.isNotBlank(message))
        {
            sb.append("\n");
        }
        sb.append(message);
    }

    public static String currentThreadToString()
    {
        return "\n" + getPartialStackTrace(null, Thread.currentThread().getStackTrace(), false);
    }


    private static String getPartialStackTrace(Set<StackTraceElement> alreadyPrinted, Throwable t, boolean onlyOrigin)
    {
        if (t != null && t.getStackTrace() != null)
        {
            return getPartialStackTrace(alreadyPrinted, t.getStackTrace(), onlyOrigin);
        }
        return "";
    }


    private static boolean isAlreadyPrinted(Set<StackTraceElement> alreadyPrinted, StackTraceElement traceElement)
    {
        if (alreadyPrinted == null || traceElement == null)
        {
            return false;
        }

        return alreadyPrinted.stream().anyMatch(traceElement::equals);
    }

    private static String getPartialStackTrace(Set<StackTraceElement> alreadyPrinted, StackTraceElement[] stackTrace, boolean onlyOrigin)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        if (stackTrace == null)
        {
            return "";
        }

        int lineNr = 1;
        for (StackTraceElement traceElement : stackTrace)
        {
            if (isAlreadyPrinted(alreadyPrinted, traceElement))
            {
                break;
            }

            String className = traceElement.getClassName();
            if (!isClassToIgnore(className))
            {
                if (lineNr == 1 || isOwnPackage(traceElement.getClassName()))
                {
                    if (alreadyPrinted != null)
                    {
                        alreadyPrinted.add(traceElement);
                    }

                    if (lineNr > 1)
                    {
                        printWriter.print("\n");
                    }
                    printWriter.print(String.format("    => %d: %s.%s(%s:%d)"
                            , lineNr
                            , traceElement.getClassName()
                            , traceElement.getMethodName()
                            , traceElement.getFileName()
                            , traceElement.getLineNumber()));
                    if (onlyOrigin && lineNr > 1)
                    {
                        break;
                    }
                }
                lineNr++;
            }
        }
        printWriter.flush();
        stringWriter.flush();
        return stringWriter.toString();
    }


    public static boolean isOwnPackage(String className)
    {
        lock.readLock().lock();
        try
        {
            return ownPackages.stream().anyMatch(className::startsWith);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }


    private static boolean isClassToIgnore(String className)
    {
        return className.equalsIgnoreCase("java.lang.Thread")
                || className.equalsIgnoreCase("hu.perit.spvitamin.core.StackTracer");
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private static String getFullStackTrace(Throwable t)
    {
        if (t == null)
        {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        t.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();

        return stringWriter.toString();
    }

    private static String removeLineSeparators(String text)
    {
        if (StringUtils.isNotBlank(text))
        {
            return text
                    .replaceAll("\r\n", "|")
                    .replaceAll("\n", "|");
        }
        else
        {
            return text;
        }
    }
}
