# spvitamin

Vitamin for Spring. A general purpose library to use in a spring based microservice environment.

## Dependencies

The spvitamin components are now available in the maven central repository.

build.gradle
```
repositories {
    mavenCentral()
}

ext {
    set('spvitaminVersion', '1.3.12-RELEASE')
}

dependencies {
    implementation 'hu.perit.spvitamin:spvitamin-core'
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

## Releases

### 1.3.13-RELEASE not yet released
- jcenter removed from build.gradle


### 1.3.12-RELEASE 2022-04-18
- CustomKeycloakAuthenticationEntryPoint changed: because of CORS restrictions, the login redirect is disabled

### 1.3.11-RELEASE 2022-04-16
- SimpleKeycloakWebSecurityConfigurerAdapter changed

### 1.3.10-RELEASE 2022-04-09
- AdminProperties defaultSiteRootFileName set to empty

### 1.3.9-RELEASE 2022-03-09
- CustomDateDeserializer, CustomLocalDateDeserializer and CustomLocalDateTimeDeserializer changed to be able to deserialize every possible date formats

### 1.3.8-RELEASE 2022-02-13
- spvitamin-spring-security-authservice-api includes spvitamin-spring-cloud-client and spvitamin-spring-cloud-feign as runtime dependencies


### 1.3.7-RELEASE 2022-02-13
- Spring cloud version updated 2020.0.2 => 2020.0.5
- Unused dependencies have been removed from spvitamin-spring-cloud-client and spvitamin-spring-general.
- Support for Zuul and Ribbon removed as these projects have been put into maintainance modus. There is a better alternative to Zuul: Spring Cloud Gateway. Please see the wstemplate project for usage.
- spvitamin-spring-cloud-feign less dependencies
- spvitamin-spring-general less dependencies, serverparameter package moved into spvitamin-spring-admin
- spvitamin-spring-logging: HTTP REQUEST is logged with '==>' instead of '>>>'
- spvitamin-spring-security-authservice: AuthClient.java moved into spvitamin-spring-security-authservice-api
- enabling stack trace in error response with the standard spring way through server.error.xxx parameters
- NPE fixed in KeystoreUtils
- RestResponseEntityExceptionHandler refactored: RestExceptionResponseFactory introduced so that it can be used in WebFlux projects as well


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
public class RepodbDbConfig {
	static final String PACKAGES = "<package name of this class>";
	static final String ENTITY_MANAGER_FACTORY = "entityManagerFactory";
	static final String TRANSACTION_MANAGER = "transactionManager";

	public static final String PERSISTENCE_UNIT = "repodb";
	private static final String DATASOURCE = "dataSource";

	private final ConnectionParam connectionParam;

	public RepodbDbConfig(DatasourceCollectionProperties dbProperties) {
		this.connectionParam = new ConnectionParam(dbProperties.getDatasource().get(PERSISTENCE_UNIT));
	}

	@Primary
	@Bean(name = DATASOURCE)
	@DependsOn("SpvitaminSpringContext") <=== springContext has to be changed to SpvitaminSpringContext from version 1.3.0 and above
	public DataSource dataSource() {
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
A bug fixed in the class AbstractInterfaceLogger. There was a failure in case of too short authorization or password header.


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
* rest (AdminApi.java, Bearer, /admin; AuthApi.java, Basic, /authenticate; KeystoreApi.java, Bearer, /keystore, /truststore;)


### spvitamin-spring-admin-api
* rest (AuthClient.java; /authenticate)


### spvitamin-spring-security
* auth
* resttemplate


## ConfigProperties

| Name                                     | Type    | Default         | Sample                              | Description |
|------------------------------------------|---------|-----------------|-------------------------------------|-------------|
| system.time-zone                         | string  | Europe/Budapest |                                     |             |
| crypto.secret                            | string  | secret          |                                     |             |
| datasource.xyz.db-type                   | string  |                 | oracle, sqlserver, mysql, etc...    |             |
| datasource.xyz.host                      | string  |                 |                                     |             |
| datasource.xyz.port                      | string  | db dependent    | e.g. at oracle: 1521, etc.          |             |
| datasource.xyz.host2                     | string  |                 |                                     | optional alternative host |
| datasource.xyz.port2                     | string  |                 |                                     | optional alternative port |
| datasource.xyz.db-name                   | string  |                 |                                     |             |
| datasource.xyz.username                  | string  |                 |                                     |             |
| datasource.xyz.encrypted-password        | string  |                 |                                     |             |
| datasource.xyz.dialect                   | string  |                 |                                     |             |
| datasource.xyz.max-pool-size             | int     | 10              |                                     |             |
| datasource.xyz.connection-timeout        | long    | 90.000 ms       |                                     |             |
| datasource.xyz.leak-detection-threashold | long    | 0               |                                     |             |
| datasource.xyz.socket-timeout            | long    | 100.000 ms      |                                     |             |
| datasource.xyz.ddl-auto                  | string  | none            | update, validate                    |             |
| jwt.private-key-alias                    | string  | -               | templatekey                         |             |
| jwt.private-key-encryptedPassword        | string  | -               | jdP5CKDIu5v2VUafF33pPQ==            |             |
| jwt.public-key-alias                     | string  | -               | templatekey                         |             |
| jwt.expiration-in-minutes                | string  | -               | 60                                  |             |
| metrics.performance-itemcount            | int     | 50              |                                     |             |
| metrics.timeout-millis                   | long    | 2.000           |                                     |             |
| metrics.metrics-gathering-hysteresis-millis| long  | 30.000          |                                     |             |
| security.admin-user-name                 | string  | -               | admin                               |             |
| security.admin-user-encryptedPassword    | string  | -               | 7MmoozfTexI=                        |             |
| security.allowed-origins                 | string  | -               |                                     |             |
| security.allowed-headers                 | string  | -               |                                     |             |
| security.allowed-methods                 | string  | -               |                                     |             |
| security.swagger-access                  | string  | *               |                                     |             |
| security.management-endpoints-access     | string  | *               |                                     |             |
| security.admin-gui-access                | string  | *               |                                     |             |
| security.admin-endpoints-access          | string  | *               |                                     |             |
| server.fqdn                              | string  | localhost       |                                     |             |
| server.port                              | int     | 8080            |                                     |             |
| server.ssl.enabled                       | boolean | FALSE           |                                     |             |
| server.ssl.key-store                     | string  | -               | classpath:jks/server-keystore.jks   |             |
| server.ssl.key-store-password            | string  | -               | changeit                            |             |
| server.ssl.key-alias                     | string  | -               | templatekey                         |             |
| server.ssl.key-password                  | string  | -               | changeit                            |             |
| server.ssl.trust-store                   | string  | -               | classpath:jks/client-truststore.jks |             |
| server.ssl.trust-store-password          | string  | -               | changeit                            |             |
| server.ssl.ignore-certificate-validation | boolean | FALSE           |                                     |             |
| server.error.includeException            | boolean | TRUE            |                                     |             |
| server.error.includeStacktrace           | string  | ALWAYS          | ALWAYS, NEVER                       |             |
| server.error.includeMessage              | string  | ALWAYS          | ALWAYS, NEVER                       | The message part is only displayed if exception is not enabled |
| ldaps.ad\<i\>.enabled                    | boolean | TRUE            |                                     |             |
| ldaps.ad\<i\>.url                        | string  |                 | ldap://192.168.62.150:10389         |             |
| ldaps.ad\<i\>.root-dn                    | string  |                 | OU=Users,DC=perit,DC=hu             |             |
| ldaps.ad\<i\>.filter                     | string  |                 | (&(objectClass=user)(userPrincipalName={0})) |             |
| ldaps.ad\<i\>.userprincipal-with-domain  | boolean | FALSE           |                                     |             |
| ldaps.ad\<i\>.domain                     | string  |                 | perit.hu                            |             |
| ldaps.ad\<i\>.connect-timeout-ms         | int     | 1000            |                                     |             |
| ldaps.ad\<i\>.bind-user-pattern          | string  |                 | uid={0},ou=Users,dc=perit,dc=hu     |             |


## Dependency graph
![](https://github.com/nagypet/spvitamin/blob/master/docs/images/spvitamin_dependency_graph.png)
Gethering dependent projects in settings.gradle is usually not hard, but in case of a project library we have to define not only direct dependencies, but also the dpendencies of all dependent projects. It is boylerplate code and totally unnecessary, because dependencies are already defined in our build.gradle files. I have implemented a groovy script to automate inclusion of dependent projects. It searches patterns in build.gradle files with 'compile project' and recursively includes the listed projects.

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
        this.rootDir.getParentFile().eachDirRecurse(){ dir ->
            dir.eachFileMatch({it == 'build.gradle'}, { 
                this.projectMap.put(dir.name, dir)
            })
        }
    }


    private Set<String> getDependendentProjects(File root) {
        def deps = [] as Set
        root.eachFileMatch({it == 'build.gradle'}, {
            it.eachLine {line ->
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
