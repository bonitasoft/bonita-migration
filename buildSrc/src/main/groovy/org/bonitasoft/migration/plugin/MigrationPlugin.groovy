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

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.db.CleanDbTask
import org.bonitasoft.migration.plugin.db.DatabasePluginExtension
import org.bonitasoft.migration.plugin.db.DatabaseResourcesConfigurator
import org.bonitasoft.migration.plugin.db.JdbcDriverDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test

import static org.bonitasoft.migration.plugin.PropertiesUtils.loadProperties
import static org.bonitasoft.migration.plugin.VersionUtils.*

/**
 * @author Baptiste Mesta
 */
class MigrationPlugin implements Plugin<Project> {

    List<String> allVersions

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
            xarecovery
        }

        def migrationPluginExtension = project.extensions.create("migration", MigrationPluginExtension.class)

        project.extensions.create("database", DatabasePluginExtension)

        project.afterEvaluate {
            DatabaseResourcesConfigurator.configureDatabaseResources(project)

            allVersions = getVersionList(project, migrationPluginExtension)
            defineAllJdbcDriversConfigurations(project)

            createMigrationTestsTasks(project, migrationPluginExtension)
            createIntegrationTestTasks(project)

            DatabaseResourcesConfigurator.finalizeTasksDependenciesOnDatabaseResources(project)
        }
    }

    def defineXaRecoveryConfiguration(Project project) {
        project.dependencies {
            xarecovery 'com.bonitasoft.tools.sqlserver:sqlserver-xa-recovery:1.0.1@jar'
            xarecovery JdbcDriverDependencies.sqlserver
        }
    }

    def defineAllJdbcDriversConfigurations(Project project) {
        // TODO: remove this:
        project.dependencies {
            // the following jdbc drivers are available for integration tests:
            drivers JdbcDriverDependencies.mysql
            drivers JdbcDriverDependencies.oracle
            drivers JdbcDriverDependencies.postgres
            drivers JdbcDriverDependencies.sqlserver
        }

        // Creating a specific driver configuration for each DB vendor and bonita version:
        String dbVendor = project.extensions.database.dbvendor
        allVersions.each { version ->
            Configuration configuration = project.configurations.create(getDatabaseDriverConfigurationName(dbVendor, version))
            configuration.dependencies.add(getDatabaseDriverDependency(project, dbVendor, version))
            project.logger.info "Creating database configuration: $configuration"
        }
    }

    def static getDatabaseDriverConfigurationName(String dbVendor, String bonitaVersion) {
        "${dbVendor}_${underscored(bonitaVersion)}" // Example: mysql_7_8_4, postgres_7_9_0
    }

    static Configuration getDatabaseDriverConfiguration(Project project, String bonitaVersion) {
        return project.getConfigurations().getByName(getDatabaseDriverConfigurationName(project.extensions.database.dbvendor, bonitaVersion))
    }

    def static getDatabaseDriverDependency(Project project, String dbVendor, String bonitaVersion) {
        String dep
        switch (dbVendor) {
            case 'mysql':
                if (Version.valueOf(bonitaVersion) < Version.valueOf("7.9.0")) {
                    dep = JdbcDriverDependencies.mysql
                } else {
                    dep = JdbcDriverDependencies.mysql8
                }
                break
            case 'oracle':
                dep = JdbcDriverDependencies.oracle
                break
            case 'sqlserver':
                dep = JdbcDriverDependencies.sqlserver
                break
            default:
                dep = JdbcDriverDependencies.postgres
        }
        return createDependencyWithoutGroovy(project, dep)
    }

    // =================================================================================================================
    // for migration tests
    // =================================================================================================================
    def createMigrationTestsTasks(Project project, MigrationPluginExtension migrationPluginExtension) {
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
                TestMigrationTask, description: "test Bonita after migrating to version $version", group: "migration")
        migrationTestTask.configureBonita(project, version, isSP)
        migrationTestTask.dependsOn project.tasks.getByName("testClasses")//bug? need to have the test compiled in
        migrationTestTask
    }

    def createRunMigrationTask(Project project, String version, boolean isSP) {
        RunMigrationTask runMigrationTask = project.tasks.create(name: "migration_" + underscored(version), type:
                RunMigrationTask, description: "Run the migration step to version $version", group: 'RunMigration')
        runMigrationTask.configureBonita(version, isSP)
        runMigrationTask.dependsOn project.tasks.getByName("distZip")
        runMigrationTask
    }

    def createPrepareTestTask(Project project, String targetVersion, List<String> previousVersions, boolean isSP) {
        def previousVersion = getVersionBefore(previousVersions, targetVersion)
        def cleandb = project.tasks.create(name: "cleandb_" + underscored(targetVersion), type: CleanDbTask) {
            bonitaVersion = previousVersion
        }
        PrepareMigrationTestTask prepareTestTask = project.tasks.create(name: "prepareTestFor_" + underscored(targetVersion), type:
                PrepareMigrationTestTask)
        prepareTestTask.configureBonita(project, underscored(previousVersion),
                underscored(targetVersion),
                isSP)
        DatabasePluginExtension properties = project.extensions.getByType(DatabasePluginExtension.class)
        if (properties.dbvendor == 'sqlserver') {
            defineXaRecoveryConfiguration(project)
            def xaRecoveryTask
            try {
                xaRecoveryTask = project.tasks.getByName('xarecovery') // create it only once
            } catch (UnknownTaskException ignored) {
                xaRecoveryTask = project.tasks.create(name: "xarecovery", type: RunMsSqlserverXARecoveryTask)
                xaRecoveryTask.dependsOn cleandb
            }
            prepareTestTask.dependsOn xaRecoveryTask
        } else {
            prepareTestTask.dependsOn cleandb
        }
        if (Version.valueOf(previousVersion) < Version.valueOf("7.3.0")) {
            def unpackBonitaHome = project.task("unpackBonitaHomeFor_" + targetVersion, type: Copy) {
                from {
                    def conf = project.configurations.getByName(underscored(previousVersion))
                    project.zipTree(conf.files.find { it.name.contains("bonita-home") }.getAbsolutePath())
                }
                into new File(project.buildDir, "bonita-home-" + targetVersion)

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

    static createDependencyWithoutGroovy(Project project, String name) {
        project.dependencies.create(name) {
            exclude module: "groovy-all"
            exclude module: "sqlserver"
            exclude module: "postgresql"
            exclude module: "mysql-connector-java"
            exclude module: "ojdbc"
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

    def getTestEngineDependencyName(MigrationPluginExtension migrationPluginExtension, String version) {
        String name
        if (migrationPluginExtension.isSP) {
            // test modules changed in 7.7.0
            if (Version.valueOf(getRawVersion(version)) >= Version.valueOf("7.7.0")) {
                name = "com.bonitasoft.engine.test:bonita-integration-tests-client-sp:${version}"
            } else {
                name = "com.bonitasoft.engine.test:bonita-integration-tests-local-sp:${version}:tests"
            }
        } else {
            name = "org.bonitasoft.engine.test:bonita-server-test-utils:${version}"
        }
        name
    }

    // Bonita alpha, beta and weekly does not follow semantic versioning, so only keep 1st digits
    // Versions look like 7.7.0.alpha-07, semver compliant would be 7.7.0-alpha-07.
    // So without this extra step, we get the following error
    // "Unexpected character 'DOT(.)' at position '5', expecting '[HYPHEN, PLUS, EOI]'"
    private static String getRawVersion(String version) {
        def (major, minor, patch) = version.split('\\.')
        String rawVersion = major + '.' + minor + '.' + patch
        rawVersion
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
