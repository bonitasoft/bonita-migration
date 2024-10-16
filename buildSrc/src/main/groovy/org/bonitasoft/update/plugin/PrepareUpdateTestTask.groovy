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

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.update.plugin.db.DatabaseResourcesConfigurator
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

import static UpdatePlugin.getDatabaseDriverConfiguration
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static org.bonitasoft.update.plugin.VersionUtils.semver
/**
 * @author Baptiste Mesta.
 */
class PrepareUpdateTestTask extends JavaExec {
    @Internal
    String targetVersion
    @Internal
    boolean isSP

    String getTargetVersion() {
        return targetVersion
    }

    boolean getIsSP() {
        return isSP
    }

    String getPreviousVersion() {
        return previousVersion
    }
    @Internal
    private String previousVersion

    PrepareUpdateTestTask() {
        setDescription "Setup the engine in order to run update tests on it."
        mainClass = 'org.bonitasoft.update.filler.FillerRunner'
        setDebug System.getProperty("filler.debug") != null
    }

    @Override
    void exec() {
        def testValues = DatabaseResourcesConfigurator.getDatabaseConnectionSystemProperties(project)
        args getFillersToRun()

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

    @Internal
    def getFillersToRun() {
        def fillers = []
        def version = semver(targetVersion)
        if (version != semver("7.9.1")) {
            //special case, there is a bug in test api 7.9.0, the bonita engine rule does not work properly
            if (isSP) {
                if (version < Version.valueOf("7.11.0")) {
                    fillers.add("com.bonitasoft.update.InitializerBefore7_11_0SP")
                } else if (version < Version.valueOf("9.0.0")) {
                    fillers.add("com.bonitasoft.update.InitializerAfter7_11_0SP")
                } else {
                    fillers.add("com.bonitasoft.update.InitializerAfter8_0_0SP")
                }
            } else {
               if (version < Version.valueOf("7.11.0")) {
                    fillers.add("org.bonitasoft.update.InitializerBefore7_11_0")
                } else if (version < Version.valueOf("9.0.0")) {
                    fillers.add("org.bonitasoft.update.InitializerAfter7_11_0")
                } else {
                    fillers.add("org.bonitasoft.update.InitializerAfter8_0_0")
                }
            }
        }
        fillers.add("${isSP ? 'com' : 'org'}.bonitasoft.update.FillBeforeUpdatingTo${isSP ? 'SP' : ''}" +
                targetVersion)
        fillers
    }

    def configureBonita(Project project, String previousVersion, String targetVersion, boolean isSP) {
        this.previousVersion = previousVersion
        this.isSP = isSP
        this.targetVersion = targetVersion
        classpath(
                project.getConfigurations().named(previousVersion), // Bonita version configuration (eg. 7_8_0)
                project.sourceSets.filler.runtimeClasspath,
                getDatabaseDriverConfiguration(project, previousVersion)
        )
    }
}

