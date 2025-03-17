#!/bin/bash

#
# Copyright 2020-2025 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

copyTo() {
  cp ./gradle.properties ./$1
}

copyTo pwdtool
copyTo spvitamin-core
copyTo spvitamin-json-time
copyTo spvitamin-spring-admin
copyTo spvitamin-spring-cloud-client
copyTo spvitamin-spring-cloud-feign
copyTo spvitamin-spring-data
copyTo spvitamin-spring-general
copyTo spvitamin-spring-logging
copyTo spvitamin-spring-security
copyTo spvitamin-spring-security-authservice
copyTo spvitamin-spring-security-authservice-api
copyTo spvitamin-spring-security-keycloak
copyTo spvitamin-spring-security-ldap
copyTo spvitamin-spring-security-oauth2
copyTo spvitamin-spring-server
copyTo spvitamin-test
