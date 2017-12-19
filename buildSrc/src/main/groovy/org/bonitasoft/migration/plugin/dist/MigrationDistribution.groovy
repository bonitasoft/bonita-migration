/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.migration.plugin.dist

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.MigrationPluginExtension
import org.bonitasoft.migration.plugin.cleandb.CleanDbTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test
/**
 *
 * comes in addition of the application plugin to add bonita homes and do all common things on the migration distribution
 *
 * @author Baptiste Mesta
 */
class MigrationDistribution implements Plugin<Project> {

    static final String MIGRATION_DISTRIBUTION_PLUGIN_NAME = "migration-dist"
    static final String MIGRATION_DISTRIBUTION_GROUP = MIGRATION_DISTRIBUTION_PLUGIN_NAME

    static final String TASK_ADD_BONITA_HOMES = "addBonitaHomes"
    static final String TASK_ADD_VERSION_IN_DIST = "addVersionsToTheDistribution"
    static final String TASK_INTEGRATION_TEST = "integrationTest"


    @Override
    void apply(Project project) {
        project.applicationDefaultJvmArgs = ["-Dfile.encoding=UTF-8"]

        project.configurations {
            drivers
        }
        project.distributions {
            main {
                contents {
                    from('build/homes') {
                        into "bonita-home"
                    }
                }
            }
        }
        defineExtraSourceSets(project)
        project.afterEvaluate {
            def configuration = project.extensions.getByType(MigrationPluginExtension.class)
            configuration.currentVersionModifier = loadProperties(project.file
                    (configuration
                            .migrationProperties)).getProperty("currentVersionModifier")
            project.mainClassName = "${configuration.isSP ? 'com' : 'org'}.bonitasoft.migration.core.Migration"
            defineConfigurations(project, configuration)
            defineDependencies(project, configuration)
            defineTasks(project, configuration)
            defineTaskDependencies(project, configuration)
        }
    }

    def loadProperties(File propertiesFile) {
        def props = new Properties()
        propertiesFile.withInputStream {
            props.load(it)
        }
        return props
    }

    private void defineTasks(Project project, MigrationPluginExtension configuration) {
        //Define tasks
        project.task(TASK_ADD_BONITA_HOMES, type: Copy) {
            group = MIGRATION_DISTRIBUTION_GROUP
            description = "Get all bonita home for each version and put it in the distribution"
            getVersionList(project, configuration).findAll { (Version.valueOf(it) < Version.valueOf("7.3.0")) }.collect {
                version ->
                from(project.configurations."config_$version".files[0].getParent()) {
                    include project.configurations."config_$version".files[0].getName()
                    rename 'bonita-home-(sp-)?([0-9\\.]+[0-9])(.[A-Z1-9]+)?(-full)?.zip', 'bonita-home-$1' + version + '$4.zip'
                }
            }
            into new File(project.projectDir, 'src/main/resources/homes')
        }
        project.task(TASK_ADD_VERSION_IN_DIST, type: AddVersionsToTheDistributionTask) {
            group = MIGRATION_DISTRIBUTION_GROUP
            description = "Put possible bonita version inside the distribution"
            versionsToAdd = getVersionList(project, configuration)
            propertiesFile = new File(project.projectDir, 'src/main/resources/bonita-versions.properties')
        }

        project.task(TASK_INTEGRATION_TEST, type: Test) {
            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
            classpath = project.sourceSets.integrationTest.runtimeClasspath
            reports.html.destination = project.file("${project.buildDir}/reports/integrationTests")
        }

        project.tasks.clean.doLast {
            def homesDirectory = new File(project.projectDir, "src/main/resources/homes")
            if (homesDirectory.exists())
                homesDirectory.eachFile {
                    it.delete()
                }

            def versionsFile = new File(project.projectDir, "src/main/resources/bonita-versions.properties")
            if (versionsFile.exists()) versionsFile.delete()
        }
    }

    static def getVersionList(project, configuration) {
        // use normalize to ensure the file content has only LF eol which is then used to split the lines (ex: manage
        // Windows CRLF checkout)
        project.file(configuration.versionListFile).text.normalize().split("\n").toList()
    }

    private void defineTaskDependencies(Project project, MigrationPluginExtension configuration) {

        def setSystemPropertiesForEngine = {
            systemProperties = [
                    "db.vendor"     : String.valueOf(project.database.properties.dbvendor),
                    "db.url"        : String.valueOf(project.database.properties.dburl),
                    "db.user"       : String.valueOf(project.database.properties.dbuser),
                    "db.password"   : String.valueOf(project.database.properties.dbpassword),
                    "db.driverClass": String.valueOf(project.database.properties.dbdriverClass),
                    "bonita.home"   : String.valueOf(project.rootProject.buildDir.absolutePath + File.separator + "bonita-home"),
            ]
        }

        project.tasks.integrationTest {
            doFirst setSystemPropertiesForEngine
        }
        project.tasks.processResources.dependsOn project.tasks.addBonitaHomes
        project.tasks.processResources.dependsOn project.tasks.addVersionsToTheDistribution
        def cleanDb = project.task("cleandb_" + (configuration.isSP ? "com" : "org"), type: CleanDbTask)
        project.tasks.integrationTest.dependsOn cleanDb
    }

    private defineDependencies(Project project, MigrationPluginExtension configuration) {
        project.dependencies {
            getVersionList(project, configuration).each {
                add("config_$it",
                        "org.bonitasoft.console:bonita-home${configuration.isSP ? '-sp' : ''}" +
                                ":${getVersion(project, configuration, it)}" +
                                ":${configuration.isSP ? '' : 'full'}@zip")
            }
            drivers group: 'org.postgresql', name: 'postgresql', version: '9.3-1102-jdbc41'
            drivers group: 'mysql', name: 'mysql-connector-java', version: '5.1.26'
            drivers group: 'com.oracle', name: 'ojdbc', version: '6'
            drivers group: 'com.microsoft.jdbc', name: 'sqlserver', version: '6.0.8112.100_41'
            compile group: 'org.postgresql', name: 'postgresql', version: '9.3-1102-jdbc41'
            compile group: 'mysql', name: 'mysql-connector-java', version: '5.1.26'

            integrationTestCompile project.sourceSets.main.output
            integrationTestCompile project.configurations.testCompile
            integrationTestCompile project.sourceSets.test.output
            integrationTestRuntime project.configurations.testRuntime

        }
    }

    static String getVersion(Project project, MigrationPluginExtension configuration, String version) {
        def versionList = getVersionList(project, configuration)
        return getVersion(versionList, version, configuration)
    }

    static String getVersion(List<String> versionList, String version, MigrationPluginExtension configuration) {
        if (versionList.last() == version) {
            if (configuration.currentVersionModifier != "NONE") {
                if (configuration.currentVersionModifier == "SNAPSHOT") {
                    return version + "-SNAPSHOT"
                } else {
                    //alpha, beta, rc tags have a dot here
                    return version + "." + configuration.currentVersionModifier
                }
            }
        }
        return version
    }

    private defineConfigurations(Project project, MigrationPluginExtension configuration) {
        project.configurations {
            getVersionList(project, configuration).collect { "config_$it" }.each { create it }
        }
    }

    private defineExtraSourceSets(Project project) {
        project.sourceSets {
            integrationTest {
                groovy.srcDir project.file('src/it/groovy')
                resources.srcDir project.file('src/it/resources')
            }
        }
    }
}
