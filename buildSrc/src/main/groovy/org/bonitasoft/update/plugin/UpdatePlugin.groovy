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

package org.bonitasoft.update.plugin

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.update.plugin.db.CleanDbTask
import org.bonitasoft.update.plugin.db.DatabasePluginExtension
import org.bonitasoft.update.plugin.db.DatabaseResourcesConfigurator
import org.bonitasoft.update.plugin.db.JdbcDriverDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.testing.Test

import static org.bonitasoft.update.plugin.PropertiesUtils.loadProperties
import static org.bonitasoft.update.plugin.VersionUtils.*

/**
 * @author Baptiste Mesta
 */
class UpdatePlugin implements Plugin<Project> {

    List<String> allVersions

    @Override
    void apply(Project project) {


        def itSourceSet = project.sourceSets.create('integrationTest')
        project.sourceSets.create('filler')
        project.sourceSets.create('enginetest')

        //integrationTest extends the "test" configuration
        project.configurations[itSourceSet.implementationConfigurationName].extendsFrom(project.configurations.testImplementation)
        project.configurations[itSourceSet.runtimeOnlyConfigurationName].extendsFrom(project.configurations.testRuntimeOnly)

        project.configurations {
            drivers
            xarecovery
        }
        project.dependencies {
            //make integration tests depends on the project main classes
            integrationTestImplementation project
        }

        def updatePluginExtension = project.extensions.create("update", UpdatePluginExtension.class)

        project.extensions.create("database", DatabasePluginExtension)

        project.afterEvaluate {
            DatabaseResourcesConfigurator.configureDatabaseResources(project)

            allVersions = getTestableVersionList(project, updatePluginExtension)
            defineAllJdbcDriversConfigurations(project)

            createUpdateTestsTasks(project, updatePluginExtension)
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
            drivers JdbcDriverDependencies.mysql8
            drivers JdbcDriverDependencies.oracle
            drivers JdbcDriverDependencies.postgres
            drivers JdbcDriverDependencies.sqlserver
        }

        // Creating a specific driver configuration for each DB vendor and bonita version:
        String dbVendor = project.extensions.database.dbvendor
        allVersions.each { version ->
            Configuration configuration = project.configurations.create(getDatabaseDriverConfigurationName(dbVendor, version))
            project.dependencies.add(configuration.name, getDatabaseDriverDependency(dbVendor))
            project.logger.info "Creating database configuration: $configuration"
        }
    }

    def static getDatabaseDriverConfigurationName(String dbVendor, String bonitaVersion) {
        "${dbVendor}_${underscored(bonitaVersion)}" // Example: mysql_7_8_4, postgres_7_9_0
    }

    static Configuration getDatabaseDriverConfiguration(Project project, String bonitaVersion) {
        return project.getConfigurations().getByName(getDatabaseDriverConfigurationName(project.extensions.database.dbvendor, bonitaVersion))
    }

    def static getDatabaseDriverDependency(String dbVendor) {
        String dep
        switch (dbVendor) {
            case 'mysql':
                dep = JdbcDriverDependencies.mysql8
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
    // for update tests
    // =================================================================================================================
    def createUpdateTestsTasks(Project project, UpdatePluginExtension updatePluginExtension) {
        updatePluginExtension.currentVersionModifier = loadProperties(project.file(updatePluginExtension.updateProperties)).getProperty("currentVersionModifier")
        createConfigurationForBonitaVersion(project, allVersions.first(), updatePluginExtension, allVersions)
        List<String> previousVersions = allVersions.subList(0, allVersions.size() - 1)
        List<String> versions = allVersions.subList(1, allVersions.size())

        def allUpdateTests = project.task("allUpdateTests", description: "Run all update tests", group: "update")
        def lastUpdateTests = project.task("lastUpdateTests", description: "Run update tests of only last step", group: "update")

        def currentVersion = allVersions.last()
        versions.each {
            createConfigurationForBonitaVersion(project, it, updatePluginExtension, allVersions)
            PrepareUpdateTestTask prepareTestTask = createPrepareTestTask(project, it, previousVersions,
                    updatePluginExtension.isSP)
            Task runUpdateTask = createRunUpdateTask(project, it, updatePluginExtension.isSP)
            Task updateTestTask = createTestUpdateTask(project, it, updatePluginExtension.isSP)
            runUpdateTask.dependsOn(prepareTestTask)
            updateTestTask.dependsOn(runUpdateTask)
            allUpdateTests.dependsOn(updateTestTask)
            if (it == currentVersion) {
                lastUpdateTests.dependsOn(updateTestTask)
            }
        }
        def versionWithModifier = getVersion(allVersions, currentVersion, updatePluginExtension)
        project.dependencies.add("fillerCompileOnly", getBonitaClientEngineDependency(updatePluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("fillerCompileOnly", getBonitaServerEngineDependency(updatePluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("fillerCompileOnly", getTestEngineDependencyName(updatePluginExtension, versionWithModifier, getRawVersion(currentVersion)), defaultExclude())
        project.dependencies.add("enginetestCompileOnly", getBonitaClientEngineDependency(updatePluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("enginetestCompileOnly", getBonitaServerEngineDependency(updatePluginExtension, versionWithModifier), defaultExclude())
        project.dependencies.add("enginetestCompileOnly", getTestEngineDependencyName(updatePluginExtension, versionWithModifier, getRawVersion(currentVersion)), defaultExclude())
    }

    def createTestUpdateTask(Project project, String version, boolean isSP) {
        TestUpdateTask updateTestTask = project.tasks.create(name: "testUpdate_" + underscored(version), type:
                TestUpdateTask, description: "test Bonita after updating to version $version", group: "update")
        updateTestTask.configureBonita(project, version, isSP)
        updateTestTask.dependsOn project.tasks.getByName("testClasses") //bug? need to have the test compiled in
        updateTestTask
    }

    def createRunUpdateTask(Project project, String version, boolean isSP) {
        RunUpdateTask runUpdateTask = project.tasks.create(name: "update_" + underscored(version), type:
                RunUpdateTask, description: "Run the update step to version $version", group: 'RunUpdate',
                constructorArgs: [isSP])
        runUpdateTask.configureBonita(version)
        runUpdateTask.dependsOn project.tasks.getByName("distZip")
        runUpdateTask
    }

    def createPrepareTestTask(Project project, String targetVersion, List<String> previousVersions, boolean isSP) {
        def previousVersion = getVersionBefore(previousVersions, targetVersion)
        def cleandb = project.tasks.create(name: "cleandb_" + underscored(targetVersion), type: CleanDbTask) {
            bonitaVersion = previousVersion
        }
        PrepareUpdateTestTask prepareTestTask = project.tasks.create(name: "prepareTestFor_" + underscored(targetVersion), type:
                PrepareUpdateTestTask)
        prepareTestTask.configureBonita(project, underscored(previousVersion), underscored(targetVersion), isSP)
        DatabasePluginExtension properties = project.extensions.getByType(DatabasePluginExtension.class)
        if (properties.dbvendor == 'sqlserver') {
            defineXaRecoveryConfiguration(project)
            RunMsSqlserverXARecoveryTask xaRecoveryTask
            try {
                xaRecoveryTask = project.tasks.getByName('xarecovery') // create it only once
            } catch (UnknownTaskException ignored) {
                xaRecoveryTask = project.tasks.create(name: "xarecovery", type: RunMsSqlserverXARecoveryTask)
                xaRecoveryTask.dependsOn cleandb
            }
            xaRecoveryTask.setMain("com.bonitasoft.tools.sqlserver.XARecovery")
            prepareTestTask.dependsOn xaRecoveryTask
        } else {
            prepareTestTask.dependsOn cleandb
        }
        prepareTestTask
    }

    Configuration createConfigurationForBonitaVersion(Project project, String bonitaVersion, UpdatePluginExtension extension, List<String> versionList) {
        Configuration configuration = project.configurations.create(underscored(bonitaVersion))
        def versionWithModifier = getVersion(versionList, bonitaVersion, extension)
        project.dependencies.add(configuration.name, getBonitaClientEngineDependency(extension, versionWithModifier), defaultExclude())
        project.dependencies.add(configuration.name, getBonitaServerEngineDependency(extension, versionWithModifier), defaultExclude())
        project.dependencies.add(configuration.name, getTestEngineDependencyName(extension, versionWithModifier, getRawVersion(bonitaVersion)), defaultExclude())
        return configuration
    }

    /**
     * exclude existing drivers and existing groovy version to avoid conflicts
     */
    static Closure defaultExclude() {
        return {
            exclude group: "org.codehaus.groovy" //exclude groovy-all and other groovy deps to avoid having multiple versions. The update tool declares the dependency itself
            exclude module: "sqlserver"
            exclude module: "postgresql"
            exclude module: "mysql-connector-java"
            exclude module: "ojdbc"
            exclude module: "pull-parser" // to not pull a wrong XML parser
        }
    }

    static def getBonitaClientEngineDependency(UpdatePluginExtension updatePluginExtension, version) {
        updatePluginExtension.isSP ? "com.bonitasoft.engine:bonita-client-sp:${version}" : "org.bonitasoft.engine:bonita-client:${version}"
    }


    static def getBonitaServerEngineDependency(UpdatePluginExtension updatePluginExtension, version) {
        updatePluginExtension.isSP ? "com.bonitasoft.engine:bonita-server-sp:${version}" : "org.bonitasoft.engine:bonita-server:${version}"
    }

    static def getTestEngineDependencyName(UpdatePluginExtension updatePluginExtension, String version, String rawVersion) {
        String name
        if (updatePluginExtension.isSP) {
            // test modules changed in 7.7.0
            if (Version.valueOf(rawVersion) >= Version.valueOf("7.11.0")) {
                name = "com.bonitasoft.engine:bonita-test-api-sp:${version}"
            } else {
                name = "com.bonitasoft.engine.test:bonita-integration-tests-client-sp:${version}"
            }
        } else {
            if (Version.valueOf(rawVersion) >= Version.valueOf("7.11.0")) {
                name = "org.bonitasoft.engine:bonita-test-api:${version}"
            } else {
                name = "org.bonitasoft.engine.test:bonita-server-test-utils:${version}"
            }
        }
        name
    }

    // Bonita alpha, beta and weekly versions do not follow semantic versioning, so only keep 1st digits
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
            integrationTestCompileOnly project.sourceSets.main.output
            integrationTestCompileOnly project.configurations.testImplementation
            integrationTestCompileOnly project.sourceSets.test.output
            integrationTestCompileOnly project.configurations.drivers
            integrationTestRuntimeOnly project.configurations.testRuntimeOnly
        }
    }

    private void defineIntegrationTestTask(Project project) {

        project.task('integrationTest', type: Test) {
            group = 'Verification'
            description = 'Run integration tests.'

            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
            classpath = project.configurations[project.sourceSets.integrationTest.runtimeClasspathConfigurationName] + project.sourceSets.integrationTest.output
            reports.html.destination = project.file("${project.buildDir}/reports/integrationTests")

            doFirst {
                systemProperties DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
            }
            //use junit 5 (provided by spock 2+)
            useJUnitPlatform()
        }

    }

}
