package org.bonitasoft.migration.plugin

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.cleandb.CleanDbPluginExtension
import org.bonitasoft.migration.plugin.cleandb.CleanDbTask
import org.bonitasoft.migration.plugin.dist.MigrationDistribution
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy

import static org.bonitasoft.migration.plugin.VersionUtils.underscored
/**
 *
 *
 *
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
        }
        def migrationPluginExtension = project.extensions.create("migration", MigrationPluginExtension.class)
        project.extensions.create("database", CleanDbPluginExtension)
        project.afterEvaluate {
            // use normalize to ensure the file content has only LF eol which is then used to split the lines (ex:
            // manage Windows CRLF checkout)
            def allVersions = project.file(migrationPluginExtension.versionListFile).text.normalize().split("\n").toList()
            migrationPluginExtension.currentVersionModifier = loadProperties(project.file
                    (migrationPluginExtension
                            .migrationProperties)).getProperty("currentVersionModifier")
            createConfigurationForBonitaVersion(project, allVersions.first(), migrationPluginExtension, allVersions)
            List<String> previousVersions = allVersions.subList(0, allVersions.size() - 1)
            List<String> versions = allVersions.subList(1, allVersions.size())
            def currentVersion = allVersions.last()
            def allMigrationTests = project.task("allMigrationTests", description: "Run all migration tests", group: "migration")
            versions.each {
                createConfigurationForBonitaVersion(project, it, migrationPluginExtension, allVersions)
                PrepareTestTask prepareTestTask = createPrepareTestTask(project, it, previousVersions,
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
    }

    def loadProperties(File propertiesFile) {
        def props = new Properties()
        propertiesFile.withInputStream {
            props.load(it)
        }
        return props
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
        PrepareTestTask prepareTestTask = project.tasks.create(name: "prepareTestFor_" + underscored(targetVersion), type:
                PrepareTestTask)
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

    def getVersionBefore(List<String> previousVersions, String targetVersion) {
        for (int i = 0; i < previousVersions.size(); i++) {
            if (previousVersions.get(i).equals(targetVersion)) {
                return previousVersions.get(i - 1)
            }
        }
        if (Version.valueOf(targetVersion) > Version.valueOf(previousVersions.last())) {
            return previousVersions.last()
        }
        throw new IllegalStateException("no previous version for $targetVersion")
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
        def version = MigrationDistribution.getVersion(versionList, bonitaVersion, migrationPluginExtension)
        return "org.bonitasoft.console:bonita-home${migrationPluginExtension.isSP ? '-sp' : ''}:${version}:${migrationPluginExtension.isSP ? '' : 'full'}@zip"
    }


    def getEngineDependency(Project project, MigrationPluginExtension migrationPluginExtension, String bonitaVersion, List<String> versionList) {
        def version = MigrationDistribution.getVersion(versionList, bonitaVersion, migrationPluginExtension)
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
        def version = MigrationDistribution.getVersion(versionList, bonitaVersion, migrationPluginExtension)
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
}
