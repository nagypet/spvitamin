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

package hu.perit.spvitamin.spring.security.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

/**
 * @author Peter Nagy
 */


@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException, ServletException
    {
        try
        {
            HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
            if (handler == null)
            {
                handlerExceptionResolver.resolveException(request, response, null, new NoResourceFoundException(
                        HttpMethod.valueOf(request.getMethod()),
                        request.getRequestURI(),
                        request.getServletPath()
                ));
                return;
            }
        }
        catch (Exception ignored)
        {
            // ha a mapping lookup maga hibázik, maradjon az eredeti viselkedés
        }
        handlerExceptionResolver.resolveException(request, response, null, ex);
    }
}
