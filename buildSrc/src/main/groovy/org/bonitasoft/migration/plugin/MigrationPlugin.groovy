/**
 * Copyright (C) 2015-2018 Bonitasoft S.A.
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

package org.bonitasoft.migration.plugin

import static org.bonitasoft.migration.plugin.PropertiesUtils.loadProperties
import static org.bonitasoft.migration.plugin.VersionUtils.getVersion
import static org.bonitasoft.migration.plugin.VersionUtils.getVersionBefore
import static org.bonitasoft.migration.plugin.VersionUtils.getVersionList
import static org.bonitasoft.migration.plugin.VersionUtils.underscored

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.db.CleanDbTask
import org.bonitasoft.migration.plugin.db.DatabasePluginExtension
import org.bonitasoft.migration.plugin.db.DatabaseResourcesConfigurator
import org.bonitasoft.migration.plugin.db.JdbcDriverDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test

/**
 * @author Baptiste Mesta
 */
class MigrationPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.sourceSets {
            filler {
            }
            enginetest {
            }
            integrationTest {
                groovy.srcDir project.file('src/it/groovy')
                resources.srcDir project.file('src/it/resources')
            }
        }

        project.configurations {
            drivers
        }

        defineJdbcDriversConfiguration(project)

        def migrationPluginExtension = project.extensions.create("migration", MigrationPluginExtension.class)
        project.extensions.create("database", DatabasePluginExtension)

        project.afterEvaluate {
            DatabaseResourcesConfigurator.configureDatabaseResources(project)
            createMigrationTestsTasks(project, migrationPluginExtension)
            createIntegrationTestTasks(project)

            DatabaseResourcesConfigurator.finalizeTasksDependenciesOnDatabaseResources(project)
        }
    }

    def defineJdbcDriversConfiguration(Project project) {
        project.dependencies {
            // the following jdbc drivers are available for integration and migration tests
            drivers JdbcDriverDependencies.mysql
            drivers JdbcDriverDependencies.oracle
            drivers JdbcDriverDependencies.postgres
            drivers JdbcDriverDependencies.sqlserver
        }
    }

    // =================================================================================================================
    // for migration tests
    // =================================================================================================================

    def createMigrationTestsTasks(Project project, MigrationPluginExtension migrationPluginExtension) {
        def allVersions = getVersionList(project, migrationPluginExtension)
        migrationPluginExtension.currentVersionModifier = loadProperties(project.file(migrationPluginExtension.migrationProperties)).getProperty("currentVersionModifier")
        createConfigurationForBonitaVersion(project, allVersions.first(), migrationPluginExtension, allVersions)
        List<String> previousVersions = allVersions.subList(0, allVersions.size() - 1)
        List<String> versions = allVersions.subList(1, allVersions.size())

        def allMigrationTests = project.task("allMigrationTests", description: "Run all migration tests", group: "migration")

        def currentVersion = allVersions.last()
        versions.each {
            createConfigurationForBonitaVersion(project, it, migrationPluginExtension, allVersions)
            PrepareMigrationTestTask prepareTestTask = createPrepareTestTask(project, it, previousVersions,
                    migrationPluginExtension.isSP)
            if (migrationPluginExtension.isSP) {
                prepareTestTask.dependsOn("getLicenses")
            }
            Task runMigrationTask = createRunMigrationTask(project, it, migrationPluginExtension.isSP)
            Task migrationTestTask = createTestMigrationTask(project, it, migrationPluginExtension.isSP)
            runMigrationTask.dependsOn(prepareTestTask)
            migrationTestTask.dependsOn(runMigrationTask)
            allMigrationTests.dependsOn(migrationTestTask)
        }
        getDependencies(project, "fillerCompileOnly").add(getEngineDependency(project, migrationPluginExtension,
                currentVersion, allVersions))
        getDependencies(project, "fillerCompileOnly").add(getTestEngineDependency(project,
                migrationPluginExtension, currentVersion, allVersions))
        getDependencies(project, "enginetestCompileOnly").add(getEngineDependency(project,
                migrationPluginExtension, currentVersion, allVersions))
        getDependencies(project, "enginetestCompileOnly").add(getTestEngineDependency(project,
                migrationPluginExtension, currentVersion, allVersions))
    }

    def getDependencies(Project project, String configurationName) {
        project.configurations.getByName(configurationName).dependencies
    }

    def createTestMigrationTask(Project project, String version, boolean isSP) {
        TestMigrationTask migrationTestTask = project.tasks.create(name: "testMigration_" + underscored(version), type:
                TestMigrationTask, description: "test the migration of version $version", group: "migration")
        migrationTestTask.configureBonita(project, version, isSP)
        migrationTestTask.dependsOn project.tasks.getByName("testClasses")//bug? need to have the test compiled in
        migrationTestTask
    }

    def createRunMigrationTask(Project project, String version, boolean isSP) {
        def runMigrationTask = project.tasks.create(name: "migration_" + underscored(version), type:
                RunMigrationTask)
        runMigrationTask.configureBonita(project, version, isSP)
        runMigrationTask.dependsOn project.tasks.getByName("distZip")
        runMigrationTask
    }

    def createPrepareTestTask(Project project, String targetVersion, List<String> previousVersions, boolean isSP) {
        def cleandb = project.tasks.create(name: "cleandb_" + underscored(targetVersion), type: CleanDbTask)
        PrepareMigrationTestTask prepareTestTask = project.tasks.create(name: "prepareTestFor_" + underscored(targetVersion), type:
                PrepareMigrationTestTask)
        def previousVersion = getVersionBefore(previousVersions, targetVersion)
        prepareTestTask.configureBonita(project, underscored(previousVersion),
                underscored(targetVersion),
                isSP)
        prepareTestTask.dependsOn cleandb
        if (Version.valueOf(previousVersion) < Version.valueOf("7.3.0")) {
            def unpackBonitaHome = project.task("unpackBonitaHomeFor_" + targetVersion, type: Copy) {
                from {
                    def conf = project.configurations.getByName(underscored(previousVersion))
                    project.zipTree(conf.files.find{it.name.contains("bonita-home")}.getAbsolutePath())
                }
                into new File(project.buildDir,"bonita-home-"+targetVersion)

            }
            prepareTestTask.dependsOn unpackBonitaHome
        }
        prepareTestTask
    }

    Configuration createConfigurationForBonitaVersion(Project project, String bonitaVersion, MigrationPluginExtension extension, List<String> versionList) {
        Configuration configuration = project.configurations.create(underscored(bonitaVersion))
        if (Version.valueOf(bonitaVersion) < Version.valueOf("7.3.0")) {
            configuration.dependencies.add(project.dependencies.create(getBonitaHomeDependency(extension, versionList,
                    bonitaVersion)))
        }
        configuration.dependencies.add(getEngineDependency(project, extension, bonitaVersion, versionList))
        configuration.dependencies.add(getTestEngineDependency(project, extension,
                bonitaVersion, versionList))
        return configuration
    }

    def getBonitaHomeDependency(MigrationPluginExtension migrationPluginExtension, List<String> versionList, String
            bonitaVersion) {
        def version = getVersion(versionList, bonitaVersion, migrationPluginExtension)
        return "org.bonitasoft.console:bonita-home${migrationPluginExtension.isSP ? '-sp' : ''}:${version}:${migrationPluginExtension.isSP ? '' : 'full'}@zip"
    }


    def getEngineDependency(Project project, MigrationPluginExtension migrationPluginExtension, String bonitaVersion, List<String> versionList) {
        def version = getVersion(versionList, bonitaVersion, migrationPluginExtension)
        String name = getEngineDependencyName(migrationPluginExtension, version)
        return createDependencyWithoutGroovy(project, name)
    }

    def createDependencyWithoutGroovy(Project project, String name) {
        project.dependencies.create(name){
            exclude module: "groovy-all"
        }
    }

    def getEngineDependencyName(MigrationPluginExtension migrationPluginExtension, version) {
        String name
        if (migrationPluginExtension.isSP) {
            name = "com.bonitasoft.engine:bonita-client-sp:${version}"
        } else {
            name = "org.bonitasoft.engine:bonita-client:${version}"
        }
        name
    }


    def getTestEngineDependency(Project project, MigrationPluginExtension migrationPluginExtension, String bonitaVersion, List<String> versionList) {
        String name
        def version = getVersion(versionList, bonitaVersion, migrationPluginExtension)
        name = getTestEngineDependencyName(migrationPluginExtension, version)
        return createDependencyWithoutGroovy(project, name)
    }

    def getTestEngineDependencyName(MigrationPluginExtension migrationPluginExtension, version) {
        String name
        if (migrationPluginExtension.isSP) {
            name = "com.bonitasoft.engine.test:bonita-integration-tests-local-sp:${version}:tests"
        } else {
            name = "org.bonitasoft.engine.test:bonita-server-test-utils:${version}"
        }
        name
    }

    // =================================================================================================================
    // for integration tests
    // =================================================================================================================

    private createIntegrationTestTasks(Project project) {
        defineIntegrationTestDependencies(project)
        defineIntegrationTestTask(project)
    }

    private defineIntegrationTestDependencies(Project project) {
        project.dependencies {
            integrationTestCompile project.sourceSets.main.output
            integrationTestCompile project.configurations.testCompile
            integrationTestCompile project.sourceSets.test.output
            integrationTestCompile project.configurations.drivers
            integrationTestRuntime project.configurations.testRuntime
        }
    }

    private void defineIntegrationTestTask(Project project) {
        def setSystemPropertiesForIntegrationTest = {
            systemProperties = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        }

        project.task('integrationTest', type: Test) {
            group = 'Verification'
            description = 'Run integration tests.'
            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
            classpath = project.sourceSets.integrationTest.runtimeClasspath
            reports.html.destination = project.file("${project.buildDir}/reports/integrationTests")

            doFirst setSystemPropertiesForIntegrationTest
        }

    }

}
