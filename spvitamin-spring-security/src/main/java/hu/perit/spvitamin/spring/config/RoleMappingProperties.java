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

package hu.perit.spvitamin.spring.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Component
@ConfigurationProperties
public class RoleMappingProperties
{

    private Map<String, RoleMapping> roles = new HashMap<>();

    @Getter
    @Setter
    @ToString
    public static class RoleMapping
    {

        private List<String> groups = new ArrayList<>();
        private List<String> users = new ArrayList<>();
        private List<String> includes = new ArrayList<>();

        public boolean containsGroup(String groupName)
        {
            return this.groups.contains(groupName) || this.groups.contains("*");
        }


        public boolean containsUser(String userName)
        {
            return this.users.contains(userName) || this.users.contains("*");
        }
    }

    public Set<String> getUserRoles(String userName)
    {
        Set<String> userRoles = this.roles.entrySet().stream() //
                .filter(i -> i.getValue().containsUser(userName)) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toSet());

        // Add included roles recursively
        Set<String> includedRoles = userRoles.stream() //
                .map(role -> getIncludedRoles(role))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        userRoles.addAll(includedRoles);

        return userRoles;
    }


    public Set<String> getIncludedRoles(String role)
    {

        if (!this.roles.containsKey(role))
        {
            return Collections.emptySet();
        }

        RoleMapping roleMapping = this.roles.get(role);

        Set<String> retval = new HashSet<>();
        for (String includedRole : roleMapping.includes)
        {
            retval.add(includedRole);
            retval.addAll(getIncludedRoles(includedRole));
        }

        return retval;
    }


    public Set<String> getGroupRoles(String groupName)
    {
        Set<String> groupRoles = this.roles.entrySet().stream() //
                .filter(i -> i.getValue().containsGroup(groupName)) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toSet());

        // Add included roles recursively
        Set<String> includedRoles = groupRoles.stream() //
                .map(role -> getIncludedRoles(role))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        groupRoles.addAll(includedRoles);

        return groupRoles;
    }
}
