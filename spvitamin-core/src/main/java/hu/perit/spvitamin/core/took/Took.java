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

package hu.perit.spvitamin.core.took;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @author Peter Nagy
 */

@Log4j
@Getter
public class Took implements AutoCloseable {

    protected String methodName;
    private final long startTimeMillis;
    private final boolean logAtClose;

    public Took() {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName("");
    }

    public Took(boolean logAtClose) {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = logAtClose;
        this.methodName = getCallingMethodName("");
    }

    public Took(String context) {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName(context);
    }

    public Took(Method method) {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName(method, "");
    }

    public Took(Method method, String context) {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = true;
        this.methodName = getCallingMethodName(method, context);
    }

    public Took(boolean logAtClose, String context) {
        this.startTimeMillis = System.currentTimeMillis();
        this.logAtClose = logAtClose;
        this.methodName = getCallingMethodName(context);
    }


    protected String getCallingMethodName(String context) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
        return String.format("%s.%s(%s)",
                getAbbreviatedClassName(stackTraceElement.getClassName()),
                stackTraceElement.getMethodName(),
                context);
    }

    protected String getCallingMethodName(Method method, String context) {
        return String.format("%s.%s(%s)",
                getAbbreviatedClassName(method.getDeclaringClass().getName()),
                method.getName(),
                context);
    }

    public long getDuration() {
        return System.currentTimeMillis() - this.startTimeMillis;
    }

    protected static String getAbbreviatedClassName(String className) {
        return Arrays.stream(className.split("\\.")).map(i -> i.substring(0, 2)).collect(Collectors.joining("."));
    }

    @Override
    public void close() {
        if (this.logAtClose) {
            long endTimeMillis = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            log.debug(String.format("%s took %d ms. Start time: %s, end time: %s",
                    this.methodName,
                    endTimeMillis - this.startTimeMillis,
                    sdf.format(new Date(this.startTimeMillis)),
                    sdf.format(new Date(endTimeMillis))));
        }
    }
}
