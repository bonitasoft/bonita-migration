package org.bonitasoft.migration.plugin

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static org.bonitasoft.migration.plugin.MigrationPlugin.getDatabaseDriverConfiguration
import static org.bonitasoft.migration.plugin.VersionUtils.dotted
import static org.bonitasoft.migration.plugin.VersionUtils.semver

/**
 * @author Baptiste Mesta.
 */
class PrepareMigrationTestTask extends JavaExec {

    String targetVersion
    boolean isSP
    private String previousVersion

    @Override
    void exec() {
        def testValues = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        args getFillersToRun()

        if (semver(previousVersion) < semver("7.13.0")) {
            executable = PropertiesUtils.getJava8Binary(project, this.name)
        }

        def property = project.property('org.gradle.jvmargs')
        if (property) {
            println "Using extra property 'org.gradle.jvmargs=$property'"
            jvmArgs property.toString().split(" ")
        }
        def sysProperty = System.getProperty("org.gradle.jvmargs")
        if (sysProperty) {
            println "Using extra property 'org.gradle.jvmargs=$property'"
            jvmArgs sysProperty.split(" ")
        }

        systemProperties testValues
        setDescription "Setup the engine in order to run migration tests on it."
        setMain "org.bonitasoft.migration.filler.FillerRunner"
        setDebug System.getProperty("filler.debug") != null

        AlternateJVMRunner.useAlternateJVMRunnerIfRequired(project, this)

        super.exec()
    }

    def delete(File file) {
        if (file.isFile()) {
            file.delete()
        } else if (file.isDirectory()) {
            file.eachFile { File child -> delete(child) }
            file.delete()
        }
    }

    private static void copyDir(File dirFrom, File dirTo) {
        dirTo.mkdir()
        dirFrom.eachFile(FILES) { File source -> new File(dirTo, source.getName()).bytes = source.bytes }
        dirFrom.eachFile(DIRECTORIES) { File source -> copyDir(source, new File(dirTo, source.getName())) }
    }

    def getFillersToRun() {
        def fillers = []
        if (semver(targetVersion) != semver("7.9.1")) {
            //special case, there is a bug in test api 7.9.0, the bonita engine rule does not work properly
            if (isSP) {
                if (semver(targetVersion) < Version.valueOf("7.11.0")) {
                    fillers.add("com.bonitasoft.migration.InitializerBefore7_11_0SP")
                } else {
                    fillers.add("com.bonitasoft.migration.InitializerAfter7_11_0SP")
                }
            } else {
               if (semver(targetVersion) < Version.valueOf("7.11.0")) {
                    fillers.add("org.bonitasoft.migration.InitializerBefore7_11_0")
                } else {
                    fillers.add("org.bonitasoft.migration.InitializerAfter7_11_0")
                }
            }
        }
        fillers.add("${isSP ? 'com' : 'org'}.bonitasoft.migration.FillBeforeMigratingTo${isSP ? 'SP' : ''}" +
                targetVersion)
        fillers
    }

    def configureBonita(Project project, String previousVersion, String targetVersion, boolean isSP) {
        this.previousVersion = previousVersion
        this.isSP = isSP
        this.targetVersion = targetVersion
        classpath(
                project.getConfigurations().getByName(previousVersion), // Bonita version configuration (eg. 7_8_0)
                project.sourceSets.filler.runtimeClasspath,
                getDatabaseDriverConfiguration(project, previousVersion)
        )
    }
}

