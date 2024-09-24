/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
