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

package hu.perit.spvitamin.core.domainuser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

/**
 * NetBIOS name: perit\nagypet
 * User Principal Name: nagypet@perit.hu
 *
 * @author Peter Nagy
 */

@Getter
@NoArgsConstructor
@ToString
public class DomainUser
{

    public static final DomainUser ANY = DomainUser.newInstance("*");

    private String domain = null;
    private String username = null;

    public static DomainUser newInstance(String userName)
    {
        Objects.requireNonNull(userName, "username is 'null'!");

        DomainUser user = new DomainUser();
        if (userName.contains("\\"))
        {
            String[] userNameParts = userName.split("\\\\");
            if (userNameParts.length == 2)
            {
                user.setDomain(extractNetbiosDomain(userNameParts[0]));
                user.setUsername(userNameParts[1]);
            }
            else
            {
                throw new IllegalArgumentException(String.format("Wrong username format: '%s'! Expected plain username or domain\\username or username@domain or username@domain.xyz", userName));
            }
        }
        else if (userName.contains("@"))
        {
            // User Principal Name
            String[] userNameParts = userName.split("@");
            if (userNameParts.length == 2)
            {
                user.setUsername(userNameParts[0]);
                user.setDomain(userNameParts[1]);
            }
            else
            {
                throw new IllegalArgumentException(String.format("Wrong username format: '%s'! Expected plain username or domain\\username or username@domain or username@domain.xyz", userName));
            }
        }
        else
        {
            user.setUsername(userName);
        }
        return user;
    }


    private static String extractNetbiosDomain(String domain)
    {
        if (domain != null && domain.contains("."))
        {
            domain = domain.substring(0, domain.indexOf('.'));
        }
        return domain;
    }


    private static boolean isQualifiedDomain(String domain)
    {
        return domain != null && domain.contains(".");
    }


    public String getCanonicalName()
    {
        if (this.domain == null)
        {
            return this.username;
        }

        return this.username + "@" + this.domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain != null ? domain.trim() : null;
    }

    public void setUsername(String username)
    {
        this.username = username != null ? username.trim() : null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DomainUser other))
        {
            return false;
        }

        if (isQualifiedDomain(domain) && isQualifiedDomain(other.domain))
        {
            return new EqualsBuilder()
                    .append(StringUtils.lowerCase(domain), StringUtils.lowerCase(other.domain))
                    .append(StringUtils.lowerCase(username), StringUtils.lowerCase(other.username))
                    .isEquals();
        }

        return new EqualsBuilder()
                .append(StringUtils.lowerCase(extractNetbiosDomain(domain)), StringUtils.lowerCase(extractNetbiosDomain(other.domain)))
                .append(StringUtils.lowerCase(username), StringUtils.lowerCase(other.username))
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(StringUtils.lowerCase(domain))
                .append(StringUtils.lowerCase(username))
                .toHashCode();
    }
}
