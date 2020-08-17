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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

/**
 * #know-how:custom-rest-error-response
 *
 * ServerException has two roles:
 * 1.) A checked exception can be wrapped in it. You can still use the method instanceOf() to check if the wrapped
 *     exception is of a given type.
 * 2.) When using a Feign client the exception from the server will be decoded and if possible regenerated on the
 *     client side. See: hu.perit.wstemplate.feignclient.RestExceptionResponseDecoder. However if the type of the server exception
 *     is not known on the client side, e.g. because some dependencies are not available, the original server-side
 *     exception will be converted to an instance of ServerException. This is typically the case with some low-level
 *     database exceptions which do not exist on the client side, but may be included in the cause-chain of a server exception.
 *
 * @author Peter Nagy
 */


@Getter
public class ServerException extends RuntimeException implements ServerExceptionInterface {

    private final String className;
    private final List<String> superClassNames;

    public static <T> T throwFrom(Throwable ex) {
        throw new ServerException(ex);
    }

    // This is for converting a checked exception into a ServerException
    ServerException(Throwable ex) {
        super(ex.getMessage(), ex.getCause());
        ExceptionWrapper exceptionWrapper = ExceptionWrapper.of(ex);
        this.className = exceptionWrapper.getClassName();
        this.superClassNames = exceptionWrapper.getSuperClassNames();
        this.safeSetStackTrace(ex.getStackTrace());
    }

    ServerException(ServerExceptionProperties exceptionProperties) {
        super(exceptionProperties.getMessage(), convertCauses(exceptionProperties.getCause()));
        this.className = exceptionProperties.getExceptionClass();
        this.superClassNames = exceptionProperties.getSuperClasses();
        this.safeSetStackTrace(exceptionProperties.getStackTrace());
    }

    private ServerException(ServerExceptionProperties exceptionProperties, ServerException cause) {
        super(exceptionProperties.getMessage(), cause);
        this.className = exceptionProperties.getExceptionClass();
        this.superClassNames = exceptionProperties.getSuperClasses();
        this.safeSetStackTrace(exceptionProperties.getStackTrace());
    }


    @Override
    public boolean instanceOf(Class anExceptionClass) {
        return this.superClassNames.contains(anExceptionClass.getName());
    }


    @Override
    public boolean instanceOf(String anExceptionClassName) {
        return this.superClassNames.contains(anExceptionClassName);
    }


    @Override
    public Annotation[] getAnnotations() {
        try {
            return Class.forName(this.className).getAnnotations();
        }
        catch (ClassNotFoundException e) {
            return new Annotation[0];
        }
    }


    @Override
    public String toString() {
        String s = this.className;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }


    private static ServerException convertCauses(ServerExceptionProperties cause) {
        if (cause == null) {
            return null;
        }
        else {
            ServerException serverException = new ServerException(cause, convertCauses(cause.getCause()));
            serverException.safeSetStackTrace(cause.getStackTrace());
            return serverException;
        }
    }


    private void safeSetStackTrace(StackTraceElement[] stackTrace) {
        this.setStackTrace(Objects.requireNonNullElseGet(stackTrace, () -> new StackTraceElement[0]));
    }
}
