/**
 * Copyright (C) 2015 Bonitasoft S.A.
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
package org.bonitasoft.update.plugin.dist

import org.bonitasoft.update.plugin.UpdatePluginExtension
import org.bonitasoft.update.plugin.db.JdbcDriverDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.bonitasoft.update.plugin.PropertiesUtils.loadProperties
import static org.bonitasoft.update.plugin.VersionUtils.getVersionList

/**
 * Comes in addition of the application plugin to do all common things on the update distribution
 *
 * @author Baptiste Mesta
 */
class UpdateDistribution implements Plugin<Project> {

    static final String UPDATE_DISTRIBUTION_PLUGIN_NAME = "update-dist"
    static final String UPDATE_DISTRIBUTION_GROUP = UPDATE_DISTRIBUTION_PLUGIN_NAME

    static final String TASK_ADD_VERSION_IN_DIST = "addVersionsToTheDistribution"

    @Override
    void apply(Project project) {
        project.applicationDefaultJvmArgs = ["-Dfile.encoding=UTF-8"]

        project.afterEvaluate {
            def configuration = project.extensions.getByType(UpdatePluginExtension.class)
            configuration.currentVersionModifier = loadProperties(project.file(configuration.updateProperties)).getProperty("currentVersionModifier")
            project.mainClassName = "${configuration.isSP ? 'com' : 'org'}.bonitasoft.update.core.Update"
            defineDependencies(project)
            defineTasks(project, configuration)
            defineTaskDependencies(project)
        }
    }

    private static void defineTasks(Project project, UpdatePluginExtension configuration) {
        //Define tasks
        project.tasks.register(TASK_ADD_VERSION_IN_DIST, AddVersionsToTheDistributionTask) {
            group = UPDATE_DISTRIBUTION_GROUP
            description = "Put possible bonita versions inside the distribution."
            versionsToAdd = getVersionList(project, configuration)
            propertiesFile = new File(project.projectDir, 'src/main/resources/bonita-versions.properties')
        }

        project.tasks.named("clean").configure {
            doLast {
                def versionsFile = new File(project.projectDir, "src/main/resources/bonita-versions.properties")
                if (versionsFile.exists()) versionsFile.delete()
            }
        }
    }

    private static void defineTaskDependencies(Project project) {
        project.tasks.named("processResources").configure { dependsOn project.tasks.named(TASK_ADD_VERSION_IN_DIST) }
    }

    private defineDependencies(Project project) {
        project.dependencies {
            // the following jdbc drivers will be included in the distribution:

            // We can always include this version because mysql8 non-xa driver class
            // inherits from the pre-8 version of the non-xa driver:
            runtimeOnly(JdbcDriverDependencies.mysql) {
                exclude(module: 'protobuf-java')
            }
            runtimeOnly JdbcDriverDependencies.postgres
            runtimeOnly JdbcDriverDependencies.sqlserver
            runtimeOnly(JdbcDriverDependencies.oracle) {
                exclude(module: "ons")
                exclude(module: "oraclepki")
                exclude(module: "osdt_cert")
                exclude(module: "osdt_core")
                exclude(module: "ucp")
                exclude(module: "simplefan")
            }
        }
    }

}
