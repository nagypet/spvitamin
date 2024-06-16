#!/bin/bash

copyTo() {
  cp ./gradle.properties ./$1
}

copyTo pwdtool
copyTo spvitamin-core
copyTo spvitamin-json-time
copyTo spvitamin-spring-admin
copyTo spvitamin-spring-cloud-client
copyTo spvitamin-spring-cloud-eureka
copyTo spvitamin-spring-cloud-feign
copyTo spvitamin-spring-data
copyTo spvitamin-spring-general
copyTo spvitamin-spring-logging
copyTo spvitamin-spring-security
copyTo spvitamin-spring-security-authservice
copyTo spvitamin-spring-security-authservice-api
copyTo spvitamin-spring-security-keycloak
copyTo spvitamin-spring-security-ldap
copyTo spvitamin-spring-server
