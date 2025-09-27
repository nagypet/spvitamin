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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieHelper
{
    public static void clearSessionCookie(HttpServletRequest request,
                                          HttpServletResponse response)
    {
        clearCookie("JSESSIONID", request, response);
        clearCookie("SESSION", request, response);
    }


    private static void clearCookie(String cookieName,
                                    HttpServletRequest request,
                                    HttpServletResponse response)
    {
        String contextPath = request.getContextPath();
        String path = (contextPath == null || contextPath.isEmpty()) ? "/" : contextPath;

        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, "");
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }


    public static String getCookie(String cookieName, HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0)
        {
            return null;
        }

        for (Cookie cookie : cookies)
        {
            if (StringUtils.equalsIgnoreCase(cookieName, cookie.getName()))
            {
                String value = StringUtils.trimToNull(cookie.getValue());
                if (value != null)
                {
                    return value;
                }
            }
        }

        return null;
    }
}
