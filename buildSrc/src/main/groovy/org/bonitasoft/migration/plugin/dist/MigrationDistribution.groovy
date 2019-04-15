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

import static org.bonitasoft.migration.plugin.PropertiesUtils.loadProperties
import static org.bonitasoft.migration.plugin.VersionUtils.getVersion
import static org.bonitasoft.migration.plugin.VersionUtils.getVersionList

import com.github.zafarkhaja.semver.Version
import org.bonitasoft.migration.plugin.MigrationPluginExtension
import org.bonitasoft.migration.plugin.db.JdbcDriverDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

/**
 * Comes in addition of the application plugin to add bonita homes and do all common things on the migration distribution
 *
 * @author Baptiste Mesta
 */
class MigrationDistribution implements Plugin<Project> {

    static final String MIGRATION_DISTRIBUTION_PLUGIN_NAME = "migration-dist"
    static final String MIGRATION_DISTRIBUTION_GROUP = MIGRATION_DISTRIBUTION_PLUGIN_NAME

    static final String TASK_ADD_BONITA_HOMES = "addBonitaHomes"
    static final String TASK_ADD_VERSION_IN_DIST = "addVersionsToTheDistribution"

    @Override
    void apply(Project project) {
        project.applicationDefaultJvmArgs = ["-Dfile.encoding=UTF-8"]

        project.distributions {
            main {
                contents {
                    from('build/homes') {
                        into "bonita-home"
                    }
                }
            }
        }
        project.afterEvaluate {
            def configuration = project.extensions.getByType(MigrationPluginExtension.class)
            configuration.currentVersionModifier = loadProperties(project.file(configuration.migrationProperties)).getProperty("currentVersionModifier")
            project.mainClassName = "${configuration.isSP ? 'com' : 'org'}.bonitasoft.migration.core.Migration"
            defineConfigurations(project, configuration)
            defineDependencies(project, configuration)
            defineTasks(project, configuration)
            defineTaskDependencies(project, configuration)
        }
    }

    private void defineTasks(Project project, MigrationPluginExtension configuration) {
        //Define tasks
        project.task(TASK_ADD_BONITA_HOMES, type: Copy) {
            group = MIGRATION_DISTRIBUTION_GROUP
            description = "Get all bonita home for each version and put it in the distribution."
            getVersionList(project, configuration).findAll { (Version.valueOf(it) < Version.valueOf("7.3.0")) }.collect {
                version ->
                from(project.configurations."config_$version".files[0].getParent()) {
                    include project.configurations."config_$version".files[0].getName()
                    rename 'bonita-home-(sp-)?([0-9\\.]+[0-9])(.[A-Z1-9]+)?(-full)?.zip', 'bonita-home-$1' + version + '$4.zip'
                }
            }
            into new File(project.projectDir, 'src/main/resources/homes')
        }
        project.task(TASK_ADD_VERSION_IN_DIST, type: AddVersionsToTheDistributionTask) {
            group = MIGRATION_DISTRIBUTION_GROUP
            description = "Put possible bonita version inside the distribution."
            versionsToAdd = getVersionList(project, configuration)
            propertiesFile = new File(project.projectDir, 'src/main/resources/bonita-versions.properties')
        }

        project.tasks.clean.doLast {
            def homesDirectory = new File(project.projectDir, "src/main/resources/homes")
            if (homesDirectory.exists())
                homesDirectory.eachFile {
                    it.delete()
                }

            def versionsFile = new File(project.projectDir, "src/main/resources/bonita-versions.properties")
            if (versionsFile.exists()) versionsFile.delete()
        }
    }

    private void defineTaskDependencies(Project project, MigrationPluginExtension configuration) {
        project.tasks.processResources.dependsOn project.tasks.addBonitaHomes
        project.tasks.processResources.dependsOn project.tasks.addVersionsToTheDistribution
    }

    private defineDependencies(Project project, MigrationPluginExtension configuration) {
        project.dependencies {
            getVersionList(project, configuration).each {
                add("config_$it",
                        "org.bonitasoft.console:bonita-home${configuration.isSP ? '-sp' : ''}" +
                                ":${getVersion(project, configuration, it)}" +
                                ":${configuration.isSP ? '' : 'full'}@zip")
            }

            // the following jdbc drivers will be included in the distribution:

            // We can always include this version because mysql8 non-xa driver class
            // inherits from the pre-8 version of the non-xa driver:
            runtime JdbcDriverDependencies.mysql8
            runtime JdbcDriverDependencies.postgres
        }
    }

    private defineConfigurations(Project project, MigrationPluginExtension configuration) {
        project.configurations {
            getVersionList(project, configuration).collect { "config_$it" }.each { create it }
        }
    }

}
