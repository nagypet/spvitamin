/*
 * Copyright 2020-2021 the original author or authors.
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

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@Slf4j
class DomainUserTest {

    @Test
    void equals1() {
        DomainUser user1 = DomainUser.newInstance("IDXAPI");
        DomainUser user2 = DomainUser.newInstance("IDXAPI");
        Assertions.assertEquals(user1, user2);
    }

    @Test
    void equals2() {
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        DomainUser user2 = DomainUser.newInstance("kozpont\\ps_sap_mw_T1");
        Assertions.assertEquals(user1, user2);
    }

    @Test
    void equals3() {
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        DomainUser user2 = DomainUser.newInstance("IDXAPI");
        Assertions.assertNotEquals(user1, user2);
    }

    @Test
    void equals4() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("ps_sap_mw_T1@kozpont.otp");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        Assertions.assertTrue(b);
    }

    @Test
    void equals5() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("IDXAPI");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        Assertions.assertTrue(b);
    }

    @Test
    void equals6() {
        String technicalUsers = "*";
        DomainUser user1 = DomainUser.newInstance("IDXAPI");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        Assertions.assertFalse(b);
    }

    @Test
    void equals7() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("IDXAPI3");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        Assertions.assertFalse(b);
    }


    @Test
    void equals8() {
        String technicalUsers = "IDXAPI,irfi\\nagypeter,ps_sap_mw_T1@kozpont.otp";
        DomainUser user1 = DomainUser.newInstance("kozpont\\PS_SAP_MW_T1");
        boolean b = Arrays.stream(technicalUsers.split(",")).map(DomainUser::newInstance).anyMatch(i -> {
            log.debug(String.format("Comparing %s to %s", i.toString(), user1.toString()));
            return i.equals(user1);
        });
        Assertions.assertTrue(b);
    }
}