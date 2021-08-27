package org.bonitasoft.migration.plugin

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.testing.Test

import static org.bonitasoft.migration.plugin.MigrationPlugin.getDatabaseDriverConfiguration
import static org.bonitasoft.migration.plugin.VersionUtils.dotted
import static org.bonitasoft.migration.plugin.VersionUtils.underscored

/**
 * @author Baptiste Mesta.
 */
class TestMigrationTask extends Test {

    private String bonitaVersion
    private boolean isSP

    @Input
    private String dbvendor
    @Input
    private String dburl

    @Override
    void executeTests() {
        def testValues = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        if (isSP) {
            // From 7.3.0, 'bonita.client.home' is the default key used by EngineStarterSP to retrieve licenses:
            testValues.put("bonita.client.home", String.valueOf(project.buildDir) + "/licenses")
        }
        if (Version.valueOf(bonitaVersion) <= Version.valueOf("7.3.0")) {
            testValues.put("bonita.home", String.valueOf(project.buildDir.absolutePath + File.separator +
                    "bonita-home-" + dotted(bonitaVersion) + File.separator + "bonita-home-to-migrate"))
        }
        setSystemProperties testValues

        if (Version.valueOf(bonitaVersion) < Version.valueOf("7.13.0")) {
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

        // From version 7.9.0+, use Java 11 to run migration tests:
        if (Version.valueOf(bonitaVersion) >= Version.valueOf("7.9.0")) {
            AlternateJVMRunner.useAlternateJVMRunnerIfRequired(project, this)
        }

        super.executeTests()
    }


    def configureBonita(Project project, String bonitaVersion, boolean isSP) {
        this.isSP = isSP
        this.bonitaVersion = bonitaVersion
        testClassesDirs = project.sourceSets.enginetest.output.classesDirs
        classpath = project.files(
                project.sourceSets.enginetest.runtimeClasspath,
                project.getConfigurations().getByName(underscored(bonitaVersion)),
                getDatabaseDriverConfiguration(project, bonitaVersion)
        )
        //add as input the database configuration, tests must  be relaunched when database configuration change
        dbvendor = project.extensions.database.dbvendor
        dburl = project.extensions.database.dburl

        def bonitaSemVer = Version.valueOf(bonitaVersion)
        if (bonitaSemVer < Version.valueOf("7.2.0")) {
            include "**/*Before7_2_0DefaultTest*"
        } else if(bonitaSemVer >= Version.valueOf("7.11.0")) {
            include "**/*After7_11_0DefaultTest*"
        } else if(bonitaSemVer != Version.valueOf("7.9.0")) { // 7.9.0 has a bug on test api, rule does not work
            include "**/*After7_2_0DefaultTest*"
        }
        include "**/*To" + underscored(bonitaVersion) + (isSP ? "SP" : "") + "*"
    }

}
