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

package hu.perit.spvitamin.spring.auth.filter.securitycontextremover;

import lombok.extern.log4j.Log4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Session handling could be denied in WebSecurityConfig, thogh we do not want to completely deny session handling. We
 * still want some management endpoints - e.g. swagger, actuator, etc. - to use pre-persisted security context.
 *
 * @author Peter Nagy
 */

@Log4j
public class SecurityContextRemoverFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Persisted security context removed by SecurityContextRemoverFilter");
        // Clearing persisted security context => ignoring session handling on the basis of JSESSIONID
        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
    }
}
