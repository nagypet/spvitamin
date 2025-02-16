# spvitamin

Vitamin for Spring. A general purpose library for developing spring based microservices. For demonstrating the usage
please visit my [wstemplate](https://github.com/nagypet/wstemplate) project.

## Current releases:

### 3.3.3-2-RELEASE

- SpringBoot 3.3.3
- SpingCloud 2023.0.3

### 3.3.2-1-RELEASE

- SpringBoot 3.3.2
- SpingCloud 2023.0.3

### 3.3.0-1-RELEASE

- SpringBoot 3.3.0
- SpingCloud 2023.0.2

### 3.1.5-2-RELEASE

- SpringBoot 3.1.5
- SpingCloud 2022.0.4

### 3.0.4-5-RELEASE

- SpringBoot 3.0.4
- SpingCloud 2022.0.1

### 2.0.2-RELEASE

- SpringBoot 2.4.5
- SpringBoot 2.7.7
- SpingCloud 2020.0.5
- SpingCloud 2021.0.5

## Dependencies

The spvitamin components are available in the maven central repository.

build.gradle

```
repositories {
    mavenCentral()
}

ext {
    set('spvitaminVersion', '3.3.3-2-RELEASE')
}

dependencies {
    implementation 'hu.perit.spvitamin:spvitamin-core'
    implementation 'hu.perit.spvitamin:spvitamin-json-time'
    implementation 'hu.perit.spvitamin:spvitamin-spring-admin'
    implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-client'
    implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-eureka'
    implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-feign'
    implementation 'hu.perit.spvitamin:spvitamin-spring-data'
    implementation 'hu.perit.spvitamin:spvitamin-spring-general'
    implementation 'hu.perit.spvitamin:spvitamin-spring-logging'
    implementation 'hu.perit.spvitamin:spvitamin-spring-security'
    implementation 'hu.perit.spvitamin:spvitamin-spring-security-authservice'
    implementation 'hu.perit.spvitamin:spvitamin-spring-security-authservice-api'
    implementation 'hu.perit.spvitamin:spvitamin-spring-security-keycloak'
    implementation 'hu.perit.spvitamin:spvitamin-spring-security-ldap'
    implementation 'hu.perit.spvitamin:spvitamin-spring-server'
}	

dependencyManagement {
    imports {
        mavenBom "hu.perit.spvitamin:spvitamin-dependencies:${spvitaminVersion}"
    }
}
```

## Release history

### 3.3.3-2-RELEASE not yet released

- Thing refactored
- OffsetDateTimeUtils to parse an OffsetDateTime from string
- @AutoregisterJsonType and @EnableJsonSubtypeAutoconfiguration annotations. The ObjectMapper is configured to automatically register subtypes of abstract classes
- SizeUtils for converting and parsing file sizes to and from human-readable forms
- ParallelRunner: a simple solution for running jobs parallely and propagating the ThreadContext and the SecurityContext 
- json-time module handles the form yyyy.MM.dd
- ConnectionParams handles socket-timeout in the PostgreSQL syntax
- FileNameUtils improved
- MapUtils for creating a Map from a Collection
- Profile loading at startup fixed
- pwdtool added feature --keygen for AES encryption
- ReflectionUtils.gettersOf() fixed: methods annotated with @JsonIgnore will not be returned. getClass() is not returned.
- Thing fixed: using ReflectionUtils.allPropertiesOf() instead of ReflectionUtils.propertiesOf() to get base class properties too
- LdapAuthenticationProviderConfigurer logging with INFO level
- SimpleFeignClientBuilder: logging
- ForwardingAuthRequestInterceptor: forwards incoming token
- JwtPropertiesPublic for special validation for the public key only
- spring-cloud-eureka removed
- LdapAuthenticationProvider only accepts username with domain
- AuthorizationService uses the canonical form of domain usernames, e.g. admin@perit 

### 3.3.3-1-RELEASE not yet released

- Compiled with SpringBoot 3.3.3 and SpingCloud 2023.0.3
- Gradle updated 8.8 -> 8.10.1
- CancellableJobExecutor takes Callable<Void>
- FieldMapper for nativ SQL queries
- BatchJob implements Callable<Void>
- Configuration of DefaultRestExceptionLoggerImpl fixed
- AbstractInterfaceLogger Options can be provided
- SpringEnvironment changed for unit testing
- LocalDateTimeUtils and FieldMapper improved
- spvitamin-test restructured
- server.error autoconfiguration changed
- Thing class
- RequestLogger refactored with Thing
- ReflectionUtils extended with Property class
- ServerParameterListImpl uses new Property interface from ReflectionUtils
- Things uses new Property interface from ReflectionUtils
- ReflectionUtils gettersOf, settersOf return methods alphabetically sorted

### 3.3.2-1-RELEASE not yet released

- Compiled with SpringBoot 3.3.2 and SpingCloud 2023.0.3
- SpvitaminApplication allows environment initializers
- /actuator/env disabled
- jaxb-api updated to jakarta-version
- admin-gui updated to Angular 18 and Material design
- ServerParameterList refactored
- DataSourceProperties: support for unencrypted password
- SimpleHttpSecurityBuilder support for authentication h2 endpoints
- DatasourceProperties: jdbc-url can be specified directly. (for Oracle)
- RoleMapper slightly refactored
- TimeFormatter
- SpvitaminApplication: handling the case when there is no .profiles at all
- Profile dependent system properties can be loaded from .sysproperties files

### 3.3.0-1-RELEASE not yet released

- Compiled with SpringBoot 3.3.0 and SpingCloud 2023.0.2
- FileNameUtils added
- h2-console must be enabled in the application specific security config
- /favicon.ico errors and warnings will not be logged by the DefaultRestExceptionLogger
- new modul added: spvitamin-test
- graceful shutdown configured as default

### 3.1.5-3-RELEASE not yet released

- BatchProcessor: new parameter to disable synchronous execution of the first item
- SpvitaminObjectMapper and SpvitaminSpringObjectMapper: mapper instance stored in a static
- profile loader improved
- LocalDateTimeUtils.format(OffsetDateTime timestamp) output truncated to seconds
- BatchProcessor reportProgress

### 3.1.5-2-RELEASE 2024-01-27

- springCloudVersion=2022.0.4

#### spvitamin-core

- ExceptionWrapper new version of causedBy
- ExceptionGuard
- StateMachine
- LocalDateTimeUtils
- LongUtils new function: `long get(Long l)`
- ListUtils

#### spvitamin-spring-general

- ResponseEntityUtils
- InMemoryMultipartFile
- Loading active profiles from: default.profiles and <hostname>.profiles

#### spvitamin-spring-security

- Some security filters improved

#### spvitamin-spring-data

- SqlServer JDBC Url can contain an `instance` part: "jdbc:sqlserver://db_host;instanceName=inst1;databaseName=test"
- New property in DatabaseProperties: `options`. This can be used for Sql-Server and H2 to define additional connection
  options.
- OffsetDateTimeToUTCConverter

#### spvitamin-spring-cloud-feign

- RestExceptionResponseDecoder improved

### 3.1.5-1-RELEASE 2023-12-13

- SpvitaminWebSecurityConfig updated using MvcRequestMatcher.Builder
- HttpLoggingFilter bug fixed: @Component annotation removed
- Json package: added support for Instant
- Json package refactored to allow microsecond precision
- Json time module extracted to new spvitamin-module: spvitamin-json-time
- Major refactor of the json time module
- InMemoryCache added

### 3.0.4-5-RELEASE 2023-11-19

- Added support for OffsetDateTime in hu/perit/spvitamin/spring/json package

### 3.0.4-4-RELEASE 2023-11-01

- DefaultRestExceptionResponseHandler slightly refactored
- Time deserialization improved with formats containing nanoseconds

### 3.0.4-3-RELEASE 2023-10-07

- DEFAULT_JACKSON_TIMESTAMPFORMAT changed to "yyyy-MM-dd'T'HH:mm:ss.SSS" and custom deserializers fixed
- RestExceptionResponse improved:
    - traceId,
    - also jakarta.validation.ConstraintViolationException is handled
    - Violated contrains listed in the `error` property even if the exception is instance of an ApplicationException or
      ApplicationRuntimeException but has been caused by a ConstraintViolationException

### 3.0.4-2-RELEASE 2023-05-13

- jar classifier was '-plain' which caused problems when spvitamin was used with maven.

### 3.0.4-1-RELEASE 2023-03-19

- Compiled with spring-boot 3.0.4
- Security keycloak is commented out, must be updated as well

### 2.0.2-RELEASE 2023-01-14

- Small fix so that spvitamin-defaults can be loaded with spring-boot 2.4.5 as well

### 2.0.1-RELEASE 2023-01-14

- Small fix so that spvitamin-defaults can be loaded with spring-boot 2.4.5 as well

### 2.0.0-RELEASE 2023-01-14

- Upgrade to Gradle 7.0
- Using SpringBoot 2.7.7, SpringCloud 2021.0.5
- Swagger 3.0 is not compatible with SpringBoot 2.7.x. In spvitamin there is a helper bean supporting the use of Swagger
  3.0. Please use the `@EnableSwagger3WithSpringBoot2_7` annotation on the Swagger configuration class:

```java
@Configuration
@EnableSwagger3WithSpringBoot2_7 <==

public class SwaggerConfig
{
  ...
}
```

- Using new SpringBoot defaults. Please see below.

### 1.9.1-RELEASE 2023-01-11

- All path under /actuator/health is also allowed. For cluster-based deployment /actuator/health/readiness,
  /actuator/liveness must be allowed.

### 1.9.0-RELEASE 2023-01-05

- Breaking change: backend URLs changed: /admin => /api/spvitamin/admin; /authenticate => /api/spvitamin/authenticate;
  keystore => /api/spvitamin/keystore; truststore => /api/spvitamin/truststore. Only the 5.0 version of AdminGUI is
  compatible with that version. Please upgrade also the AdminGUI when using this version.
- New properties: admin.keystore-admin-enabled, admin.copyright
- AdminGUI Swagger and api-docs links are shown correctly when swagger and api-docs URLs are customized with
  keys `springfox.documentation.swagger-ui.base-url` and `springfox.documentation.swagger.v2.path`.
- AdminGUI project moved into spvitamin. The compiled frontend must be copied into
  spvitamin-spring-admin\src\main\resources\public\admin-gui. Please remove any local copies of the AdminGUI and use
  this setup:

### 1.8.0-RELEASE 2022-12-30

- Support for K8s/OpenShift deployment. New property: server.external-url. Can be something like that: "https:
  //${APP_NAME}.${K8S_NAMESPACE}". It controls the links to Swagger, actuator, etc in the admin-gui.

### 1.7.2-RELEASE 2022-11-12

- StackTracer improved: 1.) each StackTraceElement will be printed only once. 2.) there is a toStringCompact() method,
  which prints even more compacted stack trace

### 1.7.1-RELEASE 2022-11-06

- Further improvement of CancelableJobExecutor: handling of QUEUED job state. afterExecute() is only called for started
  jobs, therefor only RUNNING jobs are put into STOPPING state when cancelling a job.

### 1.7.0-RELEASE 2022-10-01

- Further improvement of CancelableJobExecutor: the class is able to keep track of futures beeing within the shotdown
  process. There is a new function: cancelAll() to interrupt all running jobs.

### 1.6.0-RELEASE 2022-09-17

- RestExceptionResponseFactory logging is moved into DefaultRestExceptionLogger which can be overridden in the
  application, in order to achieve custom exception logging

### 1.5.0-RELEASE 2022-09-04

- New versions of CancelableJobExecutor and BatchProcessor to delegate the seurity context to child threads

### 1.4.2-RELEASE 2022-08-29

- DualMetric improved for batch processing

### 1.4.1-RELEASE 2022-08-08

- RestExceptionResponseFactory fixed: ApplicationException is logged with the right level

### 1.4.0-RELEASE 2022-07-31

- ObjectLogger
- @LoggedRestMethod annotation, which triggers an aspect for a good quality logging of the interface methods.
- AsyncAutoConfiguration, AsyncProperties
- LDAP URL is set to the AuthenticatedUser object, from where the authentication succeeded
- CancelableJobExecutor improved
- ServerException instanceOf() fixed

### 1.3.13-RELEASE 2022-06-12

- jcenter removed from build.gradle
- CancelableJobExecutor fixes

### 1.3.12-RELEASE 2022-04-18

- CustomKeycloakAuthenticationEntryPoint changed: because of CORS restrictions, the login redirect is disabled

### 1.3.11-RELEASE 2022-04-16

- SimpleKeycloakWebSecurityConfigurerAdapter changed

### 1.3.10-RELEASE 2022-04-09

- AdminProperties defaultSiteRootFileName set to empty

### 1.3.9-RELEASE 2022-03-09

- CustomDateDeserializer, CustomLocalDateDeserializer and CustomLocalDateTimeDeserializer changed to be able to
  deserialize every possible date formats

### 1.3.8-RELEASE 2022-02-13

- spvitamin-spring-security-authservice-api includes spvitamin-spring-cloud-client and spvitamin-spring-cloud-feign as
  runtime dependencies

### 1.3.7-RELEASE 2022-02-13

- Spring cloud version updated 2020.0.2 => 2020.0.5
- Unused dependencies have been removed from spvitamin-spring-cloud-client and spvitamin-spring-general.
- Support for Zuul and Ribbon removed as these projects have been put into maintainance modus. There is a better
  alternative to Zuul: Spring Cloud Gateway. Please see the wstemplate project for usage.
- spvitamin-spring-cloud-feign less dependencies
- spvitamin-spring-general less dependencies, serverparameter package moved into spvitamin-spring-admin
- spvitamin-spring-logging: HTTP REQUEST is logged with '==>' instead of '>>>'
- spvitamin-spring-security-authservice: AuthClient.java moved into spvitamin-spring-security-authservice-api
- enabling stack trace in error response with the standard spring way through server.error.xxx parameters
- NPE fixed in KeystoreUtils
- RestResponseEntityExceptionHandler refactored: RestExceptionResponseFactory introduced so that it can be used in
  WebFlux projects as well

### 1.3.6-RELEASE 2022-01-21

- A new LDAP setting
- HttpLoggingFilter improved in case of INFO level

### 1.3.5-RELEASE 2021-12-22

- No functional changes, but uploaded to the maven central repository

### 1.3.4-RELEASE 2021-12-19

- spvitamin-spring-security LocalUserProperties

### 1.3.3-RELEASE 2021-12-19

- BatchProcessor improved:
    - the first job is not removed from the input list
    - shutdown of the ExecutorService is more robust
- HttpLogger logs request and response details
  ```xml
  logback.xml
  
  <logger name="hu.perit.spvitamin.spring.httplogging" level="DEBUG" additivity="false">
      <appender-ref ref="application-message"/>
  </logger>
  ```
- KeystoreUtils improved error handling
- AbstractInterfaceLogger notifies listeners in case of a log even
- Improved exception handling at Event.fire() method
- EventWithWeakRef
- spvitamin-spring-data WriteBehindCache

### 1.3.2-RELEASE 2021-10-20

- NativeQueryRepoImpl new function: `public List<?> getResultList(String sql, List<Object> params, boolean logSql)`
- Swagger 3.0.0
- ApplicationException and ApplicationRuntimeException for localized, user friendly exception messages
- Oracle JDBC URL can now contain 2 address. In that case the jdbc connection string will be formatted as follows:

```
jdbc:oracle:thin:@(DESCRIPTION =
  (CONNECT_TIMEOUT=90)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=50)(RETRY_DELAY=3)
  (ADDRESS = (PROTOCOL = TCP)(HOST = host1)(PORT = port1 ))
  (ADDRESS = (PROTOCOL = TCP)(HOST = host2 )(PORT = port2 ))
  (CONNECT_DATA = (SERVICE_NAME = dbName ))) 
```

### 1.3.1-RELEASE 2021-08-14

- Bug in AbstractTokenAuthenticationFilter fixed

### 1.3.0-RELEASE 2021-07-12

- Changes merged back from customer project
- CodingException and ProcessingException in spvitamin-core
- DataServerParameters moved into the 'hu.perit.spvitamin.spring.data.config' package
- SpringContext has been given a new Bean name because of conflicting names in Camunda

Please change your db config as follows:

```Java

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = RepodbDbConfig.PACKAGES, //
        entityManagerFactoryRef = RepodbDbConfig.ENTITY_MANAGER_FACTORY, //
        transactionManagerRef = RepodbDbConfig.TRANSACTION_MANAGER)
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@Slf4j
public class RepodbDbConfig
{
    static final String PACKAGES = "<package name of this class>";
    static final String ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    static final String TRANSACTION_MANAGER = "transactionManager";

    public static final String PERSISTENCE_UNIT = "repodb";
    private static final String DATASOURCE = "dataSource";

    private final ConnectionParam connectionParam;

    public RepodbDbConfig(DatasourceCollectionProperties dbProperties)
    {
        this.connectionParam = new ConnectionParam(dbProperties.getDatasource().get(PERSISTENCE_UNIT));
    }

    @Primary
    @Bean(name = DATASOURCE)
    @DependsOn("SpvitaminSpringContext") <===
    springContext has
    to be
    changed to
    SpvitaminSpringContext from
    version 1.3.0
    and above

    public DataSource dataSource()
    {
        log.debug(String.format("creating DataSource for '%s'", PERSISTENCE_UNIT));

        // False bug report: Use try-with-resources or close this "DynamicDataSource" in
        // a "finally" clause
        DynamicDataSource ds = new DynamicDataSource(); // NOSONAR

        ds.setConnectionParam(this.connectionParam);

        return ds;
    }
	...
```

### 1.2.1-RELEASE 2021-07-11

A bug fixed in the class AbstractInterfaceLogger. There was a failure in case of too short authorization or password
header.

### 1.2.0-RELEASE 2021-07-03

New database config parameter to set an initial sql for the connection.

```
#-----------------------------------------------------------------------------------------------------------------------
# datasource repodb on oracle
#-----------------------------------------------------------------------------------------------------------------------
datasource.repodb.db-type=oracle
datasource.repodb.host=db_host
datasource.repodb.port=1521
datasource.repodb.db-name=ORCL
datasource.repodb.username=username
datasource.repodb.encrypted-password=LiCBRVVpyts=
datasource.repodb.ddl-auto=validate
datasource.repodb.connection-init-sql=ALTER SESSION SET CURRENT_SCHEMA=MY_SCHEMA
```

### 1.1.1-RELEASE 2021-06-06

AD Group mapping can be configured as follows.

```
#-----------------------------------------------------------------------------------------------------------------------
# AD group -> role mapping
#-----------------------------------------------------------------------------------------------------------------------
roles.ROLE_ADMIN.groups=Team1
roles.ROLE_ADMIN.includes=ROLE_PUBLIC
#roles.ROLE_ADMIN.users=nagy.peter
#roles.ROLE_PUBLIC.groups=DeliveryTeam


#-----------------------------------------------------------------------------------------------------------------------
# Role -> permission mapping
#-----------------------------------------------------------------------------------------------------------------------
rolemap.ROLE_ADMIN=BACKEND_WRITE_ACCESS
rolemap.ROLE_PUBLIC=BACKEND_READ_ACCESS
```

### 1.1.0-RELEASE 2021-05-31

## Components

### spvitamin-core

* batchprocessing
* connectablecontext
* crypto
* domainuser
* event
* exception
* invoker
* jobexecutor
* took
* InitParams
* NpCollections
* StackTracer

### spvitamin-spring-general

* admin (serverparameter)
* config (xxxProperties)
* connectablecontext
* environment
* exception
* exceptionhandler
* json
* keystore
* manifest (ManifestReader.java, ResourceUrlDecoder.java)
* metrics
* security
* time

### spvitamin-spring-server

* exceptionhandler
* info
* metrics
* GCTimer.java

### spvitamin-spring-admin

* admin (ShutdownManager.java)
* rest (AdminApi.java, Bearer, /admin; AuthApi.java, Basic, /authenticate; KeystoreApi.java, Bearer, /keystore,
  /truststore;)

### spvitamin-spring-admin-api

* rest (AuthClient.java; /authenticate)

### spvitamin-spring-security

* auth
* resttemplate

## ConfigProperties

| Name                                        | Type    | Default         | Sample                                       | Description                                                                                                                                                                                     |
|---------------------------------------------|---------|-----------------|----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| system.time-zone                            | string  | Europe/Budapest |                                              |                                                                                                                                                                                                 |
| crypto.secret                               | string  | secret          |                                              |                                                                                                                                                                                                 |
| admin.default-site-url                      | string  | /admin-gui      |                                              | If the webservice serves a frontend other than the AdminGUI                                                                                                                                     |
| admin.admin-gui-root-file-name              | string  |                 |                                              |                                                                                                                                                                                                 |
| admin.admin-gui-url                         | string  | /admin-gui      |                                              | The path where the AdminGUI is served                                                                                                                                                           |
| admin.admin-gui-root-file-name              | string  | index.html      |                                              |                                                                                                                                                                                                 |
| admin.copyright                             | string  | Peter Nagy ...  |                                              | This text will be shown on the footer of the Admin GUI                                                                                                                                          |
| admin.keystore-admin-enabled                | boolean | TRUE            |                                              | If set to false, the Keystore and Truststore menus are disabled in the AdminGUI. This is useful in case of a Kubernetes or Openshift deployment, where certificates are not managed by the app. |
| datasource.xyz.db-type                      | string  |                 | oracle, sqlserver, mysql, etc...             |                                                                                                                                                                                                 |
| datasource.xyz.host                         | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.instance                     | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.port                         | string  | db dependent    | e.g. at oracle: 1521, etc.                   |                                                                                                                                                                                                 |
| datasource.xyz.host2                        | string  |                 |                                              | optional alternative host                                                                                                                                                                       |
| datasource.xyz.port2                        | string  |                 |                                              | optional alternative port                                                                                                                                                                       |
| datasource.xyz.db-name                      | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.username                     | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.encrypted-password           | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.password                     | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.dialect                      | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.max-pool-size                | int     | 10              |                                              |                                                                                                                                                                                                 |
| datasource.xyz.connection-timeout           | long    | 90.000 ms       |                                              |                                                                                                                                                                                                 |
| datasource.xyz.leak-detection-threashold    | long    | 0               |                                              |                                                                                                                                                                                                 |
| datasource.xyz.socket-timeout               | long    | 100.000 ms      |                                              |                                                                                                                                                                                                 |
| datasource.xyz.ddl-auto                     | string  | none            | update, validate                             |                                                                                                                                                                                                 |
| datasource.xyz.options                      | string  |                 |                                              |                                                                                                                                                                                                 |
| datasource.xyz.jdbc-url                     | string  | -               |                                              | If specified, it will be user directly                                                                                                                                                          |
| jwt.private-key-alias                       | string  | -               | templatekey                                  |                                                                                                                                                                                                 |
| jwt.private-key-encryptedPassword           | string  | -               | jdP5CKDIu5v2VUafF33pPQ==                     |                                                                                                                                                                                                 |
| jwt.public-key-alias                        | string  | -               | templatekey                                  |                                                                                                                                                                                                 |
| jwt.expiration-in-minutes                   | string  | -               | 60                                           |                                                                                                                                                                                                 |
| metrics.performance-itemcount               | int     | 50              |                                              |                                                                                                                                                                                                 |
| metrics.timeout-millis                      | long    | 2.000           |                                              |                                                                                                                                                                                                 |
| metrics.metrics-gathering-hysteresis-millis | long    | 30.000          |                                              |                                                                                                                                                                                                 |
| security.admin-user-name                    | string  | -               | admin                                        |                                                                                                                                                                                                 |
| security.admin-user-encryptedPassword       | string  | -               | 7MmoozfTexI=                                 |                                                                                                                                                                                                 |
| security.allowed-origins                    | string  | -               |                                              |                                                                                                                                                                                                 |
| security.allowed-headers                    | string  | -               |                                              |                                                                                                                                                                                                 |
| security.allowed-methods                    | string  | -               |                                              |                                                                                                                                                                                                 |
| security.swagger-access                     | string  | *               |                                              |                                                                                                                                                                                                 |
| security.management-endpoints-access        | string  | *               |                                              |                                                                                                                                                                                                 |
| security.admin-gui-access                   | string  | *               |                                              |                                                                                                                                                                                                 |
| security.admin-endpoints-access             | string  | *               |                                              |                                                                                                                                                                                                 |
| server.fqdn                                 | string  | localhost       |                                              |                                                                                                                                                                                                 |
| server.port                                 | int     | 8080            |                                              |                                                                                                                                                                                                 |
| server.external-url                         | string  | -               | http://${APP_NAME}.${K8S_DOMAIN}             |                                                                                                                                                                                                 |
| server.ssl.enabled                          | boolean | FALSE           |                                              |                                                                                                                                                                                                 |
| server.ssl.key-store                        | string  | -               | classpath:jks/server-keystore.jks            |                                                                                                                                                                                                 |
| server.ssl.key-store-password               | string  | -               | changeit                                     |                                                                                                                                                                                                 |
| server.ssl.key-alias                        | string  | -               | templatekey                                  |                                                                                                                                                                                                 |
| server.ssl.key-password                     | string  | -               | changeit                                     |                                                                                                                                                                                                 |
| server.ssl.trust-store                      | string  | -               | classpath:jks/client-truststore.jks          |                                                                                                                                                                                                 |
| server.ssl.trust-store-password             | string  | -               | changeit                                     |                                                                                                                                                                                                 |
| server.ssl.ignore-certificate-validation    | boolean | FALSE           |                                              |                                                                                                                                                                                                 |
| server.error.includeException               | boolean | TRUE            |                                              |                                                                                                                                                                                                 |
| server.error.includeStacktrace              | string  | ALWAYS          | ALWAYS, NEVER                                |                                                                                                                                                                                                 |
| server.error.includeMessage                 | string  | ALWAYS          | ALWAYS, NEVER                                | The message part is only displayed if exception is not enabled                                                                                                                                  |
| ldaps.ad\<i\>.enabled                       | boolean | TRUE            |                                              |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.url                           | string  |                 | ldap://192.168.62.150:10389                  |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.root-dn                       | string  |                 | OU=Users,DC=perit,DC=hu                      |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.filter                        | string  |                 | (&(objectClass=user)(userPrincipalName={0})) |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.userprincipal-with-domain     | boolean | FALSE           |                                              |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.domain                        | string  |                 | perit.hu                                     |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.connect-timeout-ms            | int     | 1000            |                                              |                                                                                                                                                                                                 |
| ldaps.ad\<i\>.bind-user-pattern             | string  |                 | uid={0},ou=Users,dc=perit,dc=hu              |                                                                                                                                                                                                 |
| async.core-pool-size                        | int     | 10              |                                              |                                                                                                                                                                                                 |
| async.max-pool-size                         | int     | 100             |                                              |                                                                                                                                                                                                 |
| async.queue-capacity                        | int     | 1000            |                                              |                                                                                                                                                                                                 |
| async.thread-name-prefix                    | string  | async-          |                                              |                                                                                                                                                                                                 |

## SpringBoot defaults:

```yaml
#-----------------------------------------------------------------------------------------------------------------------
# Spring settings
#-----------------------------------------------------------------------------------------------------------------------
spring:
  jackson:
    serialization.write-dates-as-timestamps: false
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: ${system.time-zone}
  mvc.pathmatch.matching-strategy: ant_path_matcher
  jpa.open-in-view: false
  # To enable spring.profiles.include
  config.use-legacy-processing: true


#-----------------------------------------------------------------------------------------------------------------------
# Server settings
#-----------------------------------------------------------------------------------------------------------------------
server.tomcat.mbeanregistry.enabled: true


#-----------------------------------------------------------------------------------------------------------------------
# Swagger settings
#-----------------------------------------------------------------------------------------------------------------------
springfox:
  documentation:
    swagger-ui:
      base-url: "/docs"
    swagger:
      v2:
        path: "/docs"


#-----------------------------------------------------------------------------------------------------------------------
# Management endpoints
#-----------------------------------------------------------------------------------------------------------------------
management:
  endpoint:
    refresh.enabled: true
    health:
      show-details: always
      group:
        startup:
          include: "ping, healthIndicatorDatabase, diskSpace"
        readiness:
          include: "ping"
        liveness:
          include: "ping"
  endpoints:
    web.exposure.include: health,env,info,metrics,prometheus
  health:
    db.enabled: false
    ldap.enabled: false
    refresh.enabled: false
```

## Dependency graph

![](https://github.com/nagypet/spvitamin/blob/master/docs/images/spvitamin_dependency_graph.png)
Gethering dependent projects in settings.gradle is usually not hard, but in case of a project library we have to define
not only direct dependencies, but also the dpendencies of all dependent projects. It is boylerplate code and totally
unnecessary, because dependencies are already defined in our build.gradle files. I have implemented a groovy script to
automate inclusion of dependent projects. It searches patterns in build.gradle files with 'compile project' and
recursively includes the listed projects.

In order to use, place this script in a common folder in your project and reference it from your settings.gradle.

**include-project-dependencies.gradle**

```groovy
new ProjectConfigurer(settings).doIt()

class ProjectConfigurer {
    Map<String, File> projectMap = [:]
    File rootDir
    Settings settings

    ProjectConfigurer(Settings settings) {
        this.rootDir = settings.getRootDir()
        this.settings = settings
    }


    void doIt() {
        println "Included projects:"
        this.discoverProjects()
        Set<String> deps = this.getDependendentProjects(this.rootDir)
        this.includeDependentProjects(deps)
    }


    /**
     * Recursively searches folders starting with '../' containing a build.gradle file.
     * @return
     */
    private void discoverProjects() {
        this.rootDir.getParentFile().eachDirRecurse() { dir ->
            dir.eachFileMatch({ it == 'build.gradle' }, {
                this.projectMap.put(dir.name, dir)
            })
        }
    }


    private Set<String> getDependendentProjects(File root) {
        def deps = [] as Set
        root.eachFileMatch({ it == 'build.gradle' }, {
            it.eachLine { line ->
                if (line =~ /compile\s*project/) {
                    def matcher = line =~ /:[a-z-A-Z0-9]*/
                    if (matcher.size() == 1) {
                        def projName = matcher[0].substring(1)
                        deps += projName
                        deps += this.getDependendentProjects(this.projectMap[projName])
                    }
                }
            }
        })
        return deps
    }


    private void includeDependentProjects(Set<String> deps) {
        deps.each { projName ->
            settings.include "${projName}"
            def projDir = new File("${projectMap[projName]}")
            settings.project(":${projName}").projectDir = projDir
            println "  :${projName} => ${projDir}"
        }
    }
}
```

**settings.gradle**

```
pluginManagement {
  repositories {
    maven { url 'https://repo.spring.io/milestone' }
    gradlePluginPortal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == 'org.springframework.boot') {
        useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
      }
    }
  }
}


apply from: '../gradle/include-project-dependencies.gradle'

rootProject.name = 'spvitamin-spring-server'
```
