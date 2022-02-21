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

            allVersions = getTestableVersionList(project, migrationPluginExtension)
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
            project.dependencies.add(configuration.name, getDatabaseDriverDependency(project, dbVendor, version))
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
        return dep
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
        def lastMigrationTests = project.task("lastMigrationTests", description: "Run migration tests of only last step", group: "migration")

        def currentVersion = allVersions.last()
        versions.each {
            createConfigurationForBonitaVersion(project, it, migrationPluginExtension, allVersions)
            PrepareMigrationTestTask prepareTestTask = createPrepareTestTask(project, it, previousVersions,
                    migrationPluginExtension.isSP)
            Task runMigrationTask = createRunMigrationTask(project, it, migrationPluginExtension.isSP)
            Task migrationTestTask = createTestMigrationTask(project, it, migrationPluginExtension.isSP)
            runMigrationTask.dependsOn(prepareTestTask)
            migrationTestTask.dependsOn(runMigrationTask)
            allMigrationTests.dependsOn(migrationTestTask)
            if (it == currentVersion) {
                lastMigrationTests.dependsOn(migrationTestTask)
            }
        }
        def versionWithModifier = getVersion(allVersions, currentVersion, migrationPluginExtension)
        project.dependencies.add("fillerCompileOnly", getBonitaClientEngineDependency(migrationPluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("fillerCompileOnly", getBonitaServerEngineDependency(migrationPluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("fillerCompileOnly", getTestEngineDependencyName(migrationPluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("enginetestCompileOnly", getBonitaClientEngineDependency(migrationPluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("enginetestCompileOnly", getBonitaServerEngineDependency(migrationPluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("enginetestCompileOnly", getTestEngineDependencyName(migrationPluginExtension, versionWithModifier), defaultExclude())
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
        prepareTestTask.configureBonita(project, underscored(previousVersion), underscored(targetVersion), isSP)
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
        prepareTestTask
    }

    Configuration createConfigurationForBonitaVersion(Project project, String bonitaVersion, MigrationPluginExtension extension, List<String> versionList) {
        Configuration configuration = project.configurations.create(underscored(bonitaVersion))
        def version = getVersion(versionList, bonitaVersion, extension)
        project.dependencies.add(configuration.name, getBonitaClientEngineDependency(extension, version), defaultExclude())
        project.dependencies.add(configuration.name, getBonitaServerEngineDependency(extension, version), defaultExclude())
        project.dependencies.add(configuration.name, getTestEngineDependencyName(extension, version), defaultExclude())
        return configuration
    }

    /**
     * exclude existing drivers and existing groovy version to avoid conflicts
     */
    static Closure defaultExclude() {
        return {
            exclude group: "org.codehaus.groovy" //exclude groovy-all and other groovy deps to avoid having multiple versions. The migration tool declare the dependency itself
            exclude module: "sqlserver"
            exclude module: "postgresql"
            exclude module: "mysql-connector-java"
            exclude module: "ojdbc"
        }
    }

    static def getBonitaClientEngineDependency(MigrationPluginExtension migrationPluginExtension, version) {
        migrationPluginExtension.isSP?"com.bonitasoft.engine:bonita-client-sp:${version}": "org.bonitasoft.engine:bonita-client:${version}"
    }


    static def getBonitaServerEngineDependency(MigrationPluginExtension migrationPluginExtension, version) {
        migrationPluginExtension.isSP ?  "com.bonitasoft.engine:bonita-server-sp:${version}" : "org.bonitasoft.engine:bonita-server:${version}"
    }

    static def getTestEngineDependencyName(MigrationPluginExtension migrationPluginExtension, String version) {
        String name
        if (migrationPluginExtension.isSP) {
            // test modules changed in 7.7.0
            if (Version.valueOf(getRawVersion(version)) >= Version.valueOf("7.11.0")) {
                name = "com.bonitasoft.engine:bonita-test-api-sp:${version}"
            } else {
                name = "com.bonitasoft.engine.test:bonita-integration-tests-client-sp:${version}"
            }
        } else {
            if (Version.valueOf(getRawVersion(version)) >= Version.valueOf("7.11.0")) {
                name = "org.bonitasoft.engine:bonita-test-api:${version}"
            } else {
                name = "org.bonitasoft.engine.test:bonita-server-test-utils:${version}"
            }
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

        project.task('integrationTest', type: Test) {
            group = 'Verification'
            description = 'Run integration tests.'
            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
            classpath = project.sourceSets.integrationTest.runtimeClasspath
            reports.html.destination = project.file("${project.buildDir}/reports/integrationTests")

            doFirst {
                systemProperties DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
            }
        }

    }

}
