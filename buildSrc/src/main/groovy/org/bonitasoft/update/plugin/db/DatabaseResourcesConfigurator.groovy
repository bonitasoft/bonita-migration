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
package org.bonitasoft.update.plugin.db

import org.bonitasoft.update.plugin.UpdatePlugin
import org.bonitasoft.update.plugin.UpdatePluginExtension
import org.gradle.api.Project

import static org.bonitasoft.update.plugin.VersionUtils.padMajorVersionOn2Digits
import static org.bonitasoft.update.plugin.VersionUtils.underscored
import static org.bonitasoft.update.plugin.VersionUtils.getTestableVersionList

class DatabaseResourcesConfigurator {

    def static configureDatabaseResources(Project project) {
        String dbVendor = System.getProperty("db.vendor", "postgres")
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        extension.dbVendor = dbVendor
        DockerDatabaseContainerTasksCreator.createTasks(project, dbVendor)
    }

    def static finalizeTasksDependenciesOnDatabaseResources(Project project) {
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        def integrationTestTask = project.tasks.named('integrationTest')
        def uniqueName = "${extension.dbVendor.capitalize()}"
        def vendorConfigurationTask = project.tasks.named("${uniqueName}Configuration")
        def removeVendorContainer = project.tasks.named("remove${uniqueName}Container")

        // Integration tests
        integrationTestTask.configure { dependsOn(vendorConfigurationTask) }
        removeVendorContainer.configure { mustRunAfter(integrationTestTask) }

        // Update tests
        if (project.plugins.hasPlugin(UpdatePlugin)) {
            getVersionsToUpdate(project).each {
                String testUpdateVersion = underscored(padMajorVersionOn2Digits(it))
                String underscoredVersion = underscored(it)
                project.tasks.named("cleandb_" + underscoredVersion).configure { dependsOn(vendorConfigurationTask) }
                removeVendorContainer.configure { mustRunAfter(project.tasks.named("testUpdate_" + testUpdateVersion)) }
            }
        }
    }

    private static List<String> getVersionsToUpdate(Project project) {
        UpdatePluginExtension updatePluginExtension = project.extensions.getByType(UpdatePluginExtension.class)
        DatabasePluginExtension databasePluginExtension = project.extensions.getByType(DatabasePluginExtension.class)
        def allVersions = getTestableVersionList(project, updatePluginExtension, databasePluginExtension.dbVendor)
        allVersions.subList(1, allVersions.size())
    }

    def static getDatabaseConnectionSystemProperties(Project project) {
        DatabasePluginExtension configuration = project.extensions.getByType(DatabasePluginExtension.class)
        def dbValues = [
                "db.vendor"       : String.valueOf(configuration.dbVendor),
                "db.url"          : String.valueOf(configuration.dbUrl),
                "db.user"         : String.valueOf(configuration.dbUser),
                "db.password"     : String.valueOf(configuration.dbPassword),
                "db.driverClass"  : String.valueOf(configuration.dbDriverClass),
                "db.root.user"    : String.valueOf(configuration.dbRootUser),
                "db.root.password": String.valueOf(configuration.dbRootPassword),
                "db.server.name"  : String.valueOf(configuration.dbServerName),
                "db.server.port"  : String.valueOf(configuration.dbServerPort),
                "db.database.name": String.valueOf(configuration.dbDatabaseName),
                "auto.accept"     : "true"
        ]

        if ('oracle' == configuration.dbVendor) {
            // fix for https://community.oracle.com/message/3701989
            // http://www.thezonemanager.com/2015/07/whats-so-special-about-devurandom.html
            dbValues.put('java.security.egd', 'file:/dev/./urandom')
            // fix for ORA-01882
            dbValues.put('user.timezone', 'UTC')
        }

        dbValues
    }

}
