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

package hu.perit.spvitamin.core.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

/**
 * #know-how:custom-rest-error-response
 *
 * Whenever an exception is thrown, this data object will be exposed via any REST interface from server to client.
 * This conversion is necessary, because an object of type Throwable cannot be deserialized from Json.
 *
 * @author Peter Nagy
 */

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ServerExceptionProperties {

    public static final String STACKTRACE_ENABLED_KEY = "server.stacktrace-enabled";

    private String message;
    private String exceptionClass;
    private List<String> superClasses;
    private StackTraceElement[] stackTrace;
    private ServerExceptionProperties cause;


    public ServerExceptionProperties(Throwable exception) {
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(exception);
        this.exceptionClass = exceptionWrapper.getClassName();
        this.superClasses = exceptionWrapper.getSuperClassNames();
        this.message = StringUtils.isBlank(exception.getMessage()) ? this.exceptionClass : exception.getMessage();
        this.stackTrace = limitedStackTrace(exception.getStackTrace());
        if (exception.getCause() != null) {
            this.cause = convertCauses(exception.getCause());
        }
    }


    /**
     * If full stack trace is not allowed, we provide only the first item, the place of exception
     * @param stackTrace
     * @return
     */
    private static StackTraceElement[] limitedStackTrace(StackTraceElement[] stackTrace) {
        if (isStacktraceEnabled() || stackTrace == null || stackTrace.length == 0) {
            return stackTrace;
        }

        return new StackTraceElement[]{stackTrace[0]};
    }


    private static boolean isStacktraceEnabled() {
        return Boolean.parseBoolean(System.getProperty(STACKTRACE_ENABLED_KEY, "false"));
    }


    // This one is called from RestExceptionResponseDecoder
    public Exception toException() {
        ServerException serverException = new ServerException(this);
        try {
            if (serverException.instanceOf(Exception.class)) {
                // Try to regenerate the original exception
                Exception retval;
                Class<?> aClass = Class.forName(this.exceptionClass);
                if (this.cause != null) {
                    Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(String.class, Throwable.class);
                    retval = (Exception) declaredConstructor.newInstance(this.message, this.cause.toException());
                }
                else {
                    Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(String.class);
                    retval = (Exception) declaredConstructor.newInstance(this.message);
                }
                retval.setStackTrace(Objects.requireNonNullElseGet(stackTrace, () -> new StackTraceElement[0]));
                return retval;
            }
            return serverException;
        }
        catch (Exception ex) {
            // In case of any problem, we return back an instance of a ServerException
            return serverException;
        }
    }


    private ServerExceptionProperties(Throwable exception, ServerExceptionProperties cause) {
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(exception);
        this.exceptionClass = exceptionWrapper.getClassName();
        this.superClasses = exceptionWrapper.getSuperClassNames();
        this.message = StringUtils.isBlank(exception.getMessage()) ? this.exceptionClass : exception.getMessage();
        this.cause = cause;
    }


    private static ServerExceptionProperties convertCauses(Throwable cause) {
        if (cause == null) {
            return null;
        }
        else {
            ServerExceptionProperties serverExceptionProperties = new ServerExceptionProperties(cause, convertCauses(cause.getCause()));
            serverExceptionProperties.setStackTrace(limitedStackTrace(cause.getStackTrace()));
            return serverExceptionProperties;
        }
    }
}
