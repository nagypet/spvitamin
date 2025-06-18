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

package hu.perit.spvitamin.spring.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        String state = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if ("oauth2_state".equals(cookie.getName()))
                {
                    state = cookie.getValue();

                    // Removing cookie
                    Cookie deleteCookie = new Cookie("oauth2_state", "");
                    deleteCookie.setPath("/");
                    deleteCookie.setMaxAge(0);
                    response.addCookie(deleteCookie);

                    break;
                }
            }
        }

        if (state != null && !state.isEmpty())
        {
            log.info("Authentication successful, redirecting back to {}", state);
            // Redirect to the given URL
            getRedirectStrategy().sendRedirect(request, response, state);
        }
        else
        {
            // Default redirect
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
