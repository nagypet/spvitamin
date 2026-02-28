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

package hu.perit.spvitamin.spring.security.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrincipalUtils
{
    public static String getPrincipalName(Object principal)
    {
       if (principal instanceof UserDetails userDetails)
       {
           return userDetails.getUsername();
       }
       return principal.toString();
    }


    public static boolean isTechnicalUser(Object principal)
    {
        if (principal == null)
        {
            return false;
        }
        if (principal instanceof UserDetails userDetails)
        {
            return userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_TECHNICAL_USER"::equals);
        }
        return false;
    }
}
