package org.bonitasoft.update.plugin


import org.bonitasoft.update.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec

import javax.inject.Inject

import static UpdatePlugin.getDatabaseDriverConfiguration

/**
 * @author Baptiste Mesta.
 */
class RunUpdateTask extends JavaExec {
    @Input
    String bonitaVersion
    @Input
    boolean isSP

    @Inject
    RunUpdateTask(boolean isSP) {
        this.isSP = isSP
        mainClass = "${isSP ? 'com' : 'org'}.bonitasoft.update.core.Update"
        setDebug System.getProperty("update.debug") != null
    }

    @Override
    void exec() {
        def systemProps = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)

        // for UpdateTests, we need to allow to target a version that is forbidden, like 7.7.0:
        systemProps.put("ignore.invalid.target.version", "true") // value is not important

        systemProps.put("target.version", String.valueOf(bonitaVersion))
        systemProperties systemProps
        logger.info "execute update with properties $systemProperties"
        logger.info "using classpath:"
        classpath(
                project.sourceSets.main.output,
                project.sourceSets.main.runtimeClasspath,
                getDatabaseDriverConfiguration(project, bonitaVersion)
        )

        AlternateJVMRunner.useAlternateJVMRunnerIfRequired(project, this)

        super.exec()
    }

    def configureBonita(String bonitaVersion) {
        this.bonitaVersion = bonitaVersion
    }

}
