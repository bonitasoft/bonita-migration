package org.bonitasoft.update.plugin


import org.bonitasoft.update.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.tasks.JavaExec

import static UpdatePlugin.getDatabaseDriverConfiguration

/**
 * @author Baptiste Mesta.
 */
class RunUpdateTask extends JavaExec {

    String bonitaVersion
    boolean isSP

    @Override
    void exec() {
        def systemProps = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)

        // for UpdateTests, we need to allow to target a version that is forbidden, like 7.7.0:
        systemProps.put("ignore.invalid.target.version", "true") // value is not important

        systemProps.put("target.version", String.valueOf(bonitaVersion))
        systemProperties systemProps
        logger.info "execute update with properties $systemProperties"
        setMain "${isSP ? 'com' : 'org'}.bonitasoft.update.core.Update"
        logger.info "using classpath:"
        classpath(
                project.sourceSets.main.output,
                project.sourceSets.main.runtimeClasspath,
                getDatabaseDriverConfiguration(project, bonitaVersion)
        )
        setDebug System.getProperty("update.debug") != null

        AlternateJVMRunner.useAlternateJVMRunnerIfRequired(project, this)

        super.exec()
    }

    def configureBonita(String bonitaVersion, boolean isSP) {
        this.isSP = isSP
        this.bonitaVersion = bonitaVersion
    }

}
