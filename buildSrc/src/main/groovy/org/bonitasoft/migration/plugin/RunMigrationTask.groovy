package org.bonitasoft.migration.plugin

import javax.inject.Inject

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

import static org.bonitasoft.migration.plugin.MigrationPlugin.getDatabaseDriverConfiguration
import static org.bonitasoft.migration.plugin.VersionUtils.dotted
import static org.bonitasoft.migration.plugin.VersionUtils.semver

/**
 * @author Baptiste Mesta.
 */
class RunMigrationTask extends JavaExec {
    @Input
    String bonitaVersion
    @Input
    boolean isSP

    @Inject
    RunMigrationTask(boolean isSP) {
        this.isSP = isSP
        setMain("${isSP ? 'com' : 'org'}.bonitasoft.migration.core.Migration")
        setDebug System.getProperty("update.debug") != null
    }
    @Override
    void exec() {
        def systemProps = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)

        // for MigrationTests, we need to allow to target a version that is forbidden, like 7.7.0:
        systemProps.put("ignore.invalid.target.version", "true") // value is not important

        systemProps.put("target.version", String.valueOf(bonitaVersion))
        if (semver(bonitaVersion) <= Version.valueOf("7.3.0")) {
            systemProps.put("bonita.home", String.valueOf(project.buildDir.absolutePath + File.separator +
                    "bonita-home-" + dotted(bonitaVersion) + File.separator + "bonita-home-to-migrate"))
        }
        systemProperties systemProps
        logger.info "execute migration with properties $systemProperties"
        logger.info "using classpath:"
        classpath(
                project.sourceSets.main.output,
                project.sourceSets.main.runtimeClasspath,
                getDatabaseDriverConfiguration(project, bonitaVersion)
        )

        AlternateJVMRunner.useAlternateJVMRunnerIfRequired(project, this)

        super.exec()
    }

    def configureBonita(String bonitaVersion, boolean isSP) {
        this.isSP = isSP
        this.bonitaVersion = bonitaVersion
    }

}
