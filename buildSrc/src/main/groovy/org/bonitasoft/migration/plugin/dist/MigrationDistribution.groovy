/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.migration.plugin.dist

import org.bonitasoft.migration.plugin.MigrationPluginExtension
import org.bonitasoft.migration.plugin.db.JdbcDriverDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.bonitasoft.migration.plugin.PropertiesUtils.loadProperties
import static org.bonitasoft.migration.plugin.VersionUtils.getVersionList

/**
 * Comes in addition of the application plugin to do all common things on the migration distribution
 *
 * @author Baptiste Mesta
 */
class MigrationDistribution implements Plugin<Project> {

    static final String MIGRATION_DISTRIBUTION_PLUGIN_NAME = "migration-dist"
    static final String MIGRATION_DISTRIBUTION_GROUP = MIGRATION_DISTRIBUTION_PLUGIN_NAME

    static final String TASK_ADD_VERSION_IN_DIST = "addVersionsToTheDistribution"

    @Override
    void apply(Project project) {
        project.applicationDefaultJvmArgs = ["-Dfile.encoding=UTF-8"]

        project.afterEvaluate {
            def configuration = project.extensions.getByType(MigrationPluginExtension.class)
            configuration.currentVersionModifier = loadProperties(project.file(configuration.migrationProperties)).getProperty("currentVersionModifier")
            project.mainClassName = "${configuration.isSP ? 'com' : 'org'}.bonitasoft.migration.core.Migration"
            defineDependencies(project)
            defineTasks(project, configuration)
            defineTaskDependencies(project)
        }
    }

    private static void defineTasks(Project project, MigrationPluginExtension configuration) {
        //Define tasks
        project.task(TASK_ADD_VERSION_IN_DIST, type: AddVersionsToTheDistributionTask) {
            group = MIGRATION_DISTRIBUTION_GROUP
            description = "Put possible bonita version inside the distribution."
            versionsToAdd = getVersionList(project, configuration)
            propertiesFile = new File(project.projectDir, 'src/main/resources/bonita-versions.properties')
        }

        project.tasks.clean.doLast {
            def versionsFile = new File(project.projectDir, "src/main/resources/bonita-versions.properties")
            if (versionsFile.exists()) versionsFile.delete()
        }
    }

    private static void defineTaskDependencies(Project project) {
        project.tasks.processResources.dependsOn project.tasks.addVersionsToTheDistribution
    }

    private defineDependencies(Project project) {
        project.dependencies {
            // the following jdbc drivers will be included in the distribution:

            // We can always include this version because mysql8 non-xa driver class
            // inherits from the pre-8 version of the non-xa driver:
            runtime JdbcDriverDependencies.mysql8
            runtime JdbcDriverDependencies.postgres
            runtime JdbcDriverDependencies.sqlserver
            runtime(JdbcDriverDependencies.oracle) {
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
