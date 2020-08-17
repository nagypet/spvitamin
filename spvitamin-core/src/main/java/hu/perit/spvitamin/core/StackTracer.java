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

package hu.perit.spvitamin.core;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Peter Nagy
 */

public class StackTracer {

    private StackTracer() {
        throw new IllegalStateException("Utility class");
    }

    public static String toString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(removeLineSeparators(ex.toString()));

        appendWithLineSeparator(sb, getPartialStackTrace(ex, !(ex instanceof NullPointerException)));

        if (ex.getCause() != null) {
            appendWithLineSeparator(sb, "    caused by: " + toString(ex.getCause()));
        }
        return sb.toString();
    }


    private static void appendWithLineSeparator(StringBuilder sb, String message) {
        String lastChar = "";
        if (sb.length() > 0) {
            lastChar = sb.substring(sb.length() - 1);
        }
        if (!lastChar.equals("\n") && StringUtils.isNotBlank(message)) {
            sb.append("\n");
        }
        sb.append(message);
    }

    public static String currentThreadToString() {
        return "\n" + getPartialStackTrace(Thread.currentThread().getStackTrace(), false);
    }


    private static String getPartialStackTrace(Throwable t, boolean onlyOrigin) {
        if (t != null && t.getStackTrace() != null) {
            return getPartialStackTrace(t.getStackTrace(), onlyOrigin);
        }
        return "";
    }


    private static String getPartialStackTrace(StackTraceElement[] stackTrace, boolean onlyOrigin) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        if (stackTrace == null) {
            return "";
        }

        int lineNr = 1;
        for (StackTraceElement traceElement : stackTrace) {
            String className = traceElement.getClassName();
            if (!isClassToIgnore(className)) {
                if (lineNr == 1 || isOwnPackage(traceElement.getClassName())) {
                    if (lineNr > 1) {
                        printWriter.print("\n");
                    }
                    printWriter.print(String.format("    => %d: %s.%s(%s:%d)"
                            , lineNr
                            , traceElement.getClassName()
                            , traceElement.getMethodName()
                            , traceElement.getFileName()
                            , traceElement.getLineNumber()));
                    if (onlyOrigin && lineNr > 1) {
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


    private static boolean isOwnPackage(String className) {
        return className.startsWith("hu.perit");
    }


    private static boolean isClassToIgnore(String className) {
        return className.equalsIgnoreCase("java.lang.Thread")
                || className.equalsIgnoreCase("hu.nputil.StackTracer");
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private static String getFullStackTrace(Throwable t) {
        if (t == null) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        t.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();

        return stringWriter.toString();
    }

    private static String removeLineSeparators(String text) {
        if (StringUtils.isNotBlank(text)) {
            return text
                    .replaceAll("\r\n", "|")
                    .replaceAll("\n", "|");
        }
        else {
            return text;
        }
    }
}
