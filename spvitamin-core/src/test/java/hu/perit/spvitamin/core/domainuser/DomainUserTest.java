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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @author Peter Nagy
 */

@Slf4j
class DomainUserTest {

    @Test
    void equals1() {
        DomainUser user1 = DomainUser.newInstance("IDXAPI");
        DomainUser user2 = DomainUser.newInstance("IDXAPI");
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void equals2() {
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        DomainUser user2 = DomainUser.newInstance("kozpont\\ps_sap_mw_T1");
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void equals2B() {
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        DomainUser user2 = DomainUser.newInstance("kozpont.ignored\\ps_sap_mw_T1");
        assertThat(user1).isEqualTo(user2);
    }

    @Test
    void equals3() {
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        DomainUser user2 = DomainUser.newInstance("IDXAPI");
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void equals4() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        assertThat(b).isTrue();
    }

    @Test
    void equals5() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("IDXAPI");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        assertThat(b).isTrue();
    }

    @Test
    void equals6() {
        String technicalUsers = "*";
        DomainUser user1 = DomainUser.newInstance("IDXAPI");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        assertThat(b).isFalse();
    }

    @Test
    void equals7() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("IDXAPI3");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        assertThat(b).isFalse();
    }


    @Test
    void equals8() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("kozpont\\PS_SAP_MW_T1");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        assertThat(b).isTrue();
    }

    @Test
    void equals9() {
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        DomainUser user2 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.hu");
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void testNewInstanceWithPlainUsername() {
        // Arrange & Act
        DomainUser user = DomainUser.newInstance("username");

        // Assert
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getDomain()).isNull();
    }

    @Test
    void testNewInstanceWithNetbiosFormat() {
        // Arrange & Act
        DomainUser user = DomainUser.newInstance("domain\\username");

        // Assert
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getDomain()).isEqualTo("domain");
    }

    @Test
    void testNewInstanceWithUpnFormat() {
        // Arrange & Act
        DomainUser user = DomainUser.newInstance("username@domain.com");

        // Assert
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getDomain()).isEqualTo("domain.com");
    }

    @Test
    void testNewInstanceWithInvalidFormat() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            DomainUser.newInstance("domain\\user\\name");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            DomainUser.newInstance("user@name@domain");
        });
    }

    @Test
    void testNewInstanceWithNullUsername() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> {
            DomainUser.newInstance(null);
        });
    }

    @Test
    void testGetCanonicalName() {
        // Arrange
        DomainUser userWithDomain = DomainUser.newInstance("username@domain.com");
        DomainUser userWithoutDomain = DomainUser.newInstance("username");

        // Act & Assert
        assertThat(userWithDomain.getCanonicalName()).isEqualTo("username@domain.com");
        assertThat(userWithoutDomain.getCanonicalName()).isEqualTo("username");
    }

    @Test
    void testAnyConstant() {
        // Arrange
        DomainUser anyUser = DomainUser.ANY;

        // Act & Assert
        assertThat(anyUser.getUsername()).isEqualTo("*");
        assertThat(anyUser.getDomain()).isNull();
    }

    @Test
    void testHashCode() {
        // Arrange
        DomainUser user1 = DomainUser.newInstance("username@domain.com");
        DomainUser user2 = DomainUser.newInstance("username@domain.com");
        DomainUser user3 = DomainUser.newInstance("USERNAME@DOMAIN.COM"); // Different case
        DomainUser user4 = DomainUser.newInstance("domain\\username");
        DomainUser user5 = DomainUser.newInstance("different@domain.com");

        // Act & Assert
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isEqualTo(user3.hashCode()); // Case insensitive
        assertThat(user1.hashCode()).isNotEqualTo(user4.hashCode()); // Different domain format
        assertThat(user1.hashCode()).isNotEqualTo(user5.hashCode()); // Different username
    }

    @Test
    void testToString() {
        // Arrange
        DomainUser user = DomainUser.newInstance("username@domain.com");

        // Act & Assert
        assertThat(user.toString()).contains("username");
        assertThat(user.toString()).contains("domain.com");
    }

    @Test
    void testHashSetWithDomainUsers() {
        // Arrange
        DomainUser user1 = DomainUser.newInstance("username@domain.com");
        DomainUser user2 = DomainUser.newInstance("USERNAME@domain.com"); // Different case
        DomainUser user3 = DomainUser.newInstance("domain\\username");
        DomainUser user4 = DomainUser.newInstance("different@domain.com");

        // Act
        Set<DomainUser> userSet = new HashSet<>();
        userSet.add(user1);
        userSet.add(user2);
        userSet.add(user3);
        userSet.add(user4);

        // Assert
        // user1 and user2 should be considered equal due to case insensitivity
        assertThat(userSet).hasSize(3);
        assertThat(userSet).contains(user1, user3, user4);
    }

    @Test
    void testTrimming() {
        // Arrange & Act
        DomainUser user = DomainUser.newInstance(" username @ domain.com ");

        // Assert
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getDomain()).isEqualTo("domain.com");
    }
}
