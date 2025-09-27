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

package hu.perit.spvitamin.spring.session.strategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.util.List;

public class SpvitaminCompositeSessionAuthenticationStrategy extends CompositeSessionAuthenticationStrategy
{
    private final List<SessionAuthenticationStrategy> delegateStrategies;


    public SpvitaminCompositeSessionAuthenticationStrategy(List<SessionAuthenticationStrategy> delegateStrategies)
    {
        super(delegateStrategies);
        this.delegateStrategies = delegateStrategies;
    }


    public void onSessionPrincipalChanged(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response)
    {
        for (SessionAuthenticationStrategy delegateStrategy : this.delegateStrategies)
        {
            if (delegateStrategy instanceof PerUserTypeConcurrentSessionControlStrategy perUserTypeConcurrentSessionControlStrategy)
            {
                perUserTypeConcurrentSessionControlStrategy.onAuthentication(authentication, request, response);
            }
        }
    }
}
