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

package hu.perit.spvitamin.spring.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties
public class RoleMappingProperties {

    private Map<String, RoleMapping> roles = new HashMap<>();

    @Getter
    @Setter
    public static class RoleMapping {

        private List<String> groups = new ArrayList<>();
        private List<String> users = new ArrayList<>();

        public boolean containsGroup(String groupName) {
            return this.groups.contains(groupName) || this.groups.contains("*");
        }


        public boolean containsUser(String userName) {
            return this.users.contains(userName) || this.users.contains("*");
        }
    }

    public Set<String> getUserRoles(String userName) {
        return this.roles.entrySet().stream().filter(i -> i.getValue().containsUser(userName)).map(Map.Entry::getKey).collect(Collectors.toSet());
    }


    public Set<String> getGroupRoles(String groupName) {
        return this.roles.entrySet().stream().filter(i -> i.getValue().containsGroup(groupName)).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
}
