package org.bonitasoft.update.plugin

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.update.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.testing.Test

import static UpdatePlugin.getDatabaseDriverConfiguration
import static org.bonitasoft.update.plugin.VersionUtils.underscored

/**
 * @author Baptiste Mesta.
 */
class TestUpdateTask extends Test {
    @Internal
    private String bonitaVersion
    @Internal
    private boolean isSP
    @Input
    private String dbvendor

    String getBonitaVersion() {
        return bonitaVersion
    }

    boolean getIsSP() {
        return isSP
    }

    String getDbvendor() {
        return dbvendor
    }

    @Override
    void executeTests() {
        def testValues = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        systemProperties testValues

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
        AlternateJVMRunner.useAlternateJVMRunnerIfRequired(project, this)

        super.executeTests()
    }

    def configureBonita(Project project, String bonitaVersion, boolean isSP) {
        this.isSP = isSP
        this.bonitaVersion = bonitaVersion
        testClassesDirs = project.sourceSets.enginetest.output.classesDirs
        classpath = project.files(
                project.sourceSets.enginetest.runtimeClasspath,
                project.getConfigurations().named(underscored(bonitaVersion)),
                getDatabaseDriverConfiguration(project, bonitaVersion)
        )
        //add as input the database configuration, tests must  be relaunched when database configuration change
        dbvendor = project.extensions.database.dbvendor

        def bonitaSemVer = Version.valueOf(bonitaVersion)
        if (bonitaSemVer >= Version.valueOf("7.11.0")) {
            include "**/*After7_11_0DefaultTest*"
        } else {
            include "**/*After7_2_0DefaultTest*"
        }
        include "**/*To" + underscored(bonitaVersion) + (isSP ? "SP" : "") + "*"
        useJUnitPlatform()
    }

}
