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

package hu.perit.spvitamin.spring.info;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestQuery
{
    public static HttpServletRequest getHttpServletRequest()
    {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null)
        {
            return attributes.getRequest();
        }
        return null;
    }


    public static HttpSession getSession()
    {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (httpServletRequest != null)
        {
            return httpServletRequest.getSession(false);
        }
        return null;
    }


    public static String getSessionId()
    {
        return getSessionId(false);
    }


    public static String getSessionId(boolean createIfNotExist)
    {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (httpServletRequest != null)
        {
            HttpSession session = httpServletRequest.getSession(createIfNotExist);
            if (session != null)
            {
                return session.getId();
            }
        }
        return null;
    }


    public static boolean isFromBrowser()
    {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (httpServletRequest != null)
        {
            // Check user-agent for common browser identifiers
            String userAgent = httpServletRequest.getHeader("user-agent");
            if (StringUtils.isNotBlank(userAgent))
            {
                if (StringUtils.containsIgnoreCase(userAgent, "firefox")
                        || StringUtils.containsIgnoreCase(userAgent, "chrome")
                        || StringUtils.containsIgnoreCase(userAgent, "mozilla")
                        || StringUtils.containsIgnoreCase(userAgent, "safari")
                        || StringUtils.containsIgnoreCase(userAgent, "edge")
                        || StringUtils.containsIgnoreCase(userAgent, "opera")
                        || StringUtils.containsIgnoreCase(userAgent, "msie")
                        || StringUtils.containsIgnoreCase(userAgent, "trident")
                )
                {
                    return true;
                }
            }

            // Check for browser-specific headers
            String referer = httpServletRequest.getHeader("referer");
            String origin = httpServletRequest.getHeader("origin");
            String acceptLanguage = httpServletRequest.getHeader("accept-language");
            String acceptEncoding = httpServletRequest.getHeader("accept-encoding");

            // If referer or origin is present, it's likely a browser
            if (StringUtils.isNotBlank(referer) || StringUtils.isNotBlank(origin))
            {
                return true;
            }

            // Browsers typically send accept-language and accept-encoding headers
            if (StringUtils.isNotBlank(acceptLanguage) && StringUtils.isNotBlank(acceptEncoding))
            {
                return true;
            }

            // Check for Sec-* headers which are typically sent by modern browsers
            String secFetchDest = httpServletRequest.getHeader("sec-fetch-dest");
            String secFetchMode = httpServletRequest.getHeader("sec-fetch-mode");
            String secFetchSite = httpServletRequest.getHeader("sec-fetch-site");
            String secFetchUser = httpServletRequest.getHeader("sec-fetch-user");

            // If any of the Sec-Fetch-* headers are present, it's likely a browser
            if (StringUtils.isNotBlank(secFetchDest) ||
                    StringUtils.isNotBlank(secFetchMode) ||
                    StringUtils.isNotBlank(secFetchSite) ||
                    StringUtils.isNotBlank(secFetchUser))
            {
                return true;
            }

            // If any of the Sec-CH-UA* headers are present, it's likely a browser
            String secChUa = httpServletRequest.getHeader("sec-ch-ua");
            String secChUaMobile = httpServletRequest.getHeader("sec-ch-ua-mobile");
            String secChUaPlatform = httpServletRequest.getHeader("sec-ch-ua-platform");
            return StringUtils.isNotBlank(secChUa) ||
                    StringUtils.isNotBlank(secChUaMobile) ||
                    StringUtils.isNotBlank(secChUaPlatform);
        }
        return false;
    }
}
