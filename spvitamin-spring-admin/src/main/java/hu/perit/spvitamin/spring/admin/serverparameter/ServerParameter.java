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

package hu.perit.spvitamin.spring.admin.serverparameter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Peter Nagy
 */


@Getter
@AllArgsConstructor
public class ServerParameter implements Comparable<ServerParameter>
{
    private String name;
    private String value;
    private boolean isLink;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ServerParameter)) return false;
        ServerParameter other = (ServerParameter) o;
        return new EqualsBuilder()
                .append(StringUtils.lowerCase(name), StringUtils.lowerCase(other.name))
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(StringUtils.lowerCase(name))
                .toHashCode();
    }

    @Override
    public int compareTo(ServerParameter other)
    {
        return new CompareToBuilder()
                .append(StringUtils.lowerCase(this.name), StringUtils.lowerCase(other.name))
                .toComparison();
    }
}
