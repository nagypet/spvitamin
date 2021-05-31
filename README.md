# spvitamin

## Releases

1.1.0-RELEASE: 2021-05-31

## Build

build.gradle
```
repositories {
    jcenter()
    mavenCentral()
    maven {
        url "http://perit.hu/maven"
    }
}

ext {
	set('spvitaminVersion', '1.1.0-RELEASE')
}

dependencies {
	implementation 'hu.perit.spvitamin:spvitamin-core'
	implementation 'hu.perit.spvitamin:spvitamin-spring-admin'
	implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-client'
	implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-eureka'
	implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-feign'
	implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-ribbon'
	implementation 'hu.perit.spvitamin:spvitamin-spring-cloud-zuul	'
	implementation 'hu.perit.spvitamin:spvitamin-spring-data'
	implementation 'hu.perit.spvitamin:spvitamin-spring-general'
	implementation 'hu.perit.spvitamin:spvitamin-spring-logging'
	implementation 'hu.perit.spvitamin:spvitamin-spring-security'
	implementation 'hu.perit.spvitamin:spvitamin-spring-security-authservice'
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
| jwt.private-key-alias                    | string  | -               | templatekey                         |             |
| jwt.private-key-encryptedPassword        | string  | -               | jdP5CKDIu5v2VUafF33pPQ==            |             |
| jwt.public-key-alias                     | string  | -               | templatekey                         |             |
| jwt.expiration-in-minutes                | string  | -               | 60                                  |             |
| metrics.performance-itemcount            | int     | 50              |                                     |             |
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
