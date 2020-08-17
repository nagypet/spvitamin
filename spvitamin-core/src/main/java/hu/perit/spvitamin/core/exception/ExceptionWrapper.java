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

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles regular exceptions and ServerException transparently
 *
 * @author Peter Nagy
 */


public class ExceptionWrapper implements ServerExceptionInterface {

    private final Throwable exception;

    public static ExceptionWrapper of(Throwable exception) {
        return new ExceptionWrapper(exception);
    }


    private ExceptionWrapper(Throwable exception) {
        Objects.requireNonNull(exception, "Constructor parameter cannot be null!");
        this.exception = exception;
    }


    public Throwable getRootCause() {
        return getRootCause(this.exception);
    }


    public boolean causedBy(Class anExceptionClass) {
        return causedBy(this.exception, anExceptionClass, null);
    }


    public boolean causedBy(String anExceptionClassName) {
        return causedBy(this.exception, anExceptionClassName, null);
    }


    public boolean causedBy(Class anExceptionClass, String messageStart) {
        return causedBy(this.exception, anExceptionClass, messageStart);
    }


    public String toStringWithCauses() {
        StringBuilder sb = new StringBuilder();
        sb.append(removeLineSeparators(this.exception.toString()));

        List<Throwable> causes = getAllCauses(this.exception);
        Collections.reverse(causes);

        for (int i = 0; i < causes.size(); i++) {
            sb.append(System.lineSeparator());
            sb.append(String.join("", Collections.nCopies(i + 1, "  ")));
            sb.append("caused by " + removeLineSeparators(causes.get(i).toString()));
        }
        return sb.toString();
    }

    private static String removeLineSeparators(String text) {
        return text
                .replaceAll("\r\n", "|")
                .replaceAll("\n", "|");
    }


    public List<Throwable> getAllCauses() {
        return getAllCauses(this.exception);
    }


    public Optional<Throwable> getFromCauseChain(Class anExceptionClass) {
        List<Throwable> allCauses = this.getAllCauses();
        allCauses.add(this.exception);
        List<Throwable> throwables = allCauses.stream()
                .filter(i -> getClassName(i).equals(anExceptionClass.getName()))
                .collect(Collectors.toList());
        if (!throwables.isEmpty()) {
            return Optional.of(throwables.get(0));
        }
        else {
            return Optional.empty();
        }
    }


    private static List<Throwable> getAllCauses(Throwable root) {
        if (root.getCause() == null) {
            return new ArrayList<>();
        }
        else {
            List<Throwable> causes = getAllCauses(root.getCause());
            causes.add(root.getCause());
            return causes;
        }
    }


    private static Throwable getRootCause(Throwable root) {
        if (root.getCause() == null) {
            return root;
        }
        else {
            return getRootCause(root.getCause());
        }
    }


    private static boolean causedBy(Throwable root, Class anExceptionClass, String messageStart) {
        return causedBy(root, anExceptionClass.getName(), messageStart);
    }


    private static boolean causedBy(Throwable root, String anExceptionClassName, String messageStart) {
        if (StringUtils.isNotBlank(messageStart)) {
            if (getClassName(root).equalsIgnoreCase(anExceptionClassName)
                    && StringUtils.startsWith(root.getMessage(), messageStart)) {
                return true;
            }
            // e.g. root: InsufficientAuthenticationException
            // anExceptionClass: AuthenticationException should return true
            if (isInstanceOf(root, anExceptionClassName)
                    && StringUtils.startsWith(root.getMessage(), messageStart)) {
                return true;
            }
        }
        else {
            if (getClassName(root).equalsIgnoreCase(anExceptionClassName)) {
                return true;
            }
            // e.g. root: InsufficientAuthenticationException
            // anExceptionClass: AuthenticationException should return true
            if (isInstanceOf(root, anExceptionClassName)) {
                return true;
            }
        }

        if (root.getCause() == null) {
            return false;
        }
        else {
            return causedBy(root.getCause(), anExceptionClassName, messageStart);
        }
    }


    @Override
    public String getClassName() {
        return getClassName(this.exception);
    }


    public static String getClassName(Throwable throwable) {
        if (throwable instanceof ServerException) {
            return ((ServerException) throwable).getClassName();
        }
        else {
            return throwable.getClass().getName();
        }
    }


    @Override
    public boolean instanceOf(Class anExceptionClass) {
        return isInstanceOf(this.exception, anExceptionClass);
    }


    @Override
    public boolean instanceOf(String anExceptionClassName) {
        return isInstanceOf(this.exception, anExceptionClassName);
    }


    /**
     *
     * @param throwable It may be an instance of ServerException as well
     * @param anExceptionClass
     * @return
     */
    public static boolean isInstanceOf(Throwable throwable, Class anExceptionClass) {
        if (throwable instanceof ServerException) {
            return ((ServerException) throwable).instanceOf(anExceptionClass);
        }
        else {
            return anExceptionClass.isInstance(throwable);
        }
    }


    public static boolean isInstanceOf(Throwable throwable, String anExceptionClassName) {
        if (throwable instanceof ServerException) {
            return ((ServerException) throwable).instanceOf(anExceptionClassName);
        }
        else {
            try {
                return Class.forName(anExceptionClassName).isInstance(throwable);
            }
            catch (ClassNotFoundException e) {
                return false;
            }
        }
    }


    @Override
    public List<String> getSuperClassNames() {
        return getSuperClassNames(this.exception);
    }

    @Override
    public Annotation[] getAnnotations() {
        if (this.exception instanceof ServerException) {
            return ((ServerException) this.exception).getAnnotations();
        }
        else {
            return this.exception.getClass().getAnnotations();
        }
    }


    public static List<String> getSuperClassNames(Throwable throwable) {
        if (throwable instanceof ServerException) {
            return ((ServerException) throwable).getSuperClassNames();
        }
        else {
            return extractSuperClassNames(throwable);
        }
    }


    private static List<String> extractSuperClassNames(Throwable exception) {

        List<String> superClasses = new ArrayList<>();
        Class aClass = exception.getClass();
        while (aClass != null) {
            superClasses.add(aClass.getName());
            aClass = aClass.getSuperclass();
        }
        return superClasses;
    }
}
