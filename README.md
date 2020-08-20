# spvitamin
## Dependency graph
![](https://github.com/nagypet/spvitamin/blob/master/docs/images/spvitamin_dependency_graph.jpg)
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
