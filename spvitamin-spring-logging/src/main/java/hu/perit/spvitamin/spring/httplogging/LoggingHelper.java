/*
 * Copyright 2020-2021 the original author or authors.
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

package hu.perit.spvitamin.spring.httplogging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Set;

@Slf4j
class LoggingHelper
{
    private static final Set<String> HEADER_NAMES_TO_HIDE = Set.of("password");
    private static final Set<String> HEADER_NAMES_TO_ABBREVIATE = Set.of("authorization");

    public static void logRequestHeaders(HttpServletRequest httpRequest)
    {
        log.debug(getHeadersAsString(new HttpRequestWrapper(httpRequest)));
    }


    public static String getHeadersAsString(HttpWrapper httpWrapper)
    {
        try
        {
            Iterator<String> iterator = httpWrapper.getHeaderNames();
            StringBuilder sb = new StringBuilder();
            while (iterator.hasNext())
            {
                String headerName = iterator.next();
                sb.append(String.format("%s:%s|", headerName, hidePasswords(httpWrapper, headerName)));
            }
            return sb.toString();
        }
        catch (IllegalStateException ex)
        {
            return "HTTP headers: HTTP request is empty!";
        }
    }


    private static String hidePasswords(HttpWrapper httpWrapper, String headerName)
    {
        String headerValue = httpWrapper.getHeader(headerName);
        return getMaskedHeaderValue(headerName, headerValue);
    }


    protected static String getMaskedHeaderValue(String headerName, String headerValue)
    {
        if (HEADER_NAMES_TO_ABBREVIATE.stream().anyMatch(i -> i.equalsIgnoreCase(headerName)))
        {
            if (headerValue.length() <= 4)
            {
                return "***";
            }
            else if (headerValue.length() <= 6)
            {
                return StringUtils.abbreviate(headerValue, Math.max(4, headerValue.length() - 1));
            }
            else
            {
                return StringUtils.abbreviate(headerValue, Math.max(4, Math.min(40, (headerValue.length() / 2) + 3)));
            }
        }
        else if (HEADER_NAMES_TO_HIDE.stream().anyMatch(i -> i.equalsIgnoreCase(headerName)))
        {
            return "***";
        }
        else
        {
            return headerValue;
        }
    }
}
