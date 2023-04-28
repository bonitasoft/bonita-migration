package org.bonitasoft.update.plugin.db

import org.bonitasoft.update.plugin.UpdatePlugin
import org.bonitasoft.update.plugin.UpdatePluginExtension
import org.gradle.api.Project

import static org.bonitasoft.update.plugin.VersionUtils.underscored
import static org.bonitasoft.update.plugin.VersionUtils.getTestableVersionList

class DatabaseResourcesConfigurator {

    def static configureDatabaseResources(Project project) {
        String dbVendor = System.getProperty("db.vendor", "postgres")
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        extension.dbvendor = dbVendor
        DockerDatabaseContainerTasksCreator.createTasks(project, dbVendor)
    }

    def static finalizeTasksDependenciesOnDatabaseResources(Project project) {
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        def integrationTestTask = project.tasks.findByName('integrationTest')
        def uniqueName = "${extension.dbvendor.capitalize()}"
        def vendorConfigurationTask = project.tasks.findByName("${uniqueName}Configuration")
        def removeVendorContainer = project.tasks.findByName("remove${uniqueName}Container")

        // Integration tests
        integrationTestTask.dependsOn(vendorConfigurationTask)
        removeVendorContainer.mustRunAfter(integrationTestTask)

        // Update tests
        if (project.plugins.hasPlugin(UpdatePlugin)) {
            getVersionsToUpdate(project).each {
                String underscoredVersion = underscored(it)
                project.tasks.findByName("cleandb_" + underscoredVersion).dependsOn(vendorConfigurationTask)
                removeVendorContainer.mustRunAfter(project.tasks.findByName("testUpdate_" + underscoredVersion))
            }
        }
    }

    private static List<String> getVersionsToUpdate(Project project) {
        UpdatePluginExtension updatePluginExtension = project.extensions.getByType(UpdatePluginExtension.class)
        def allVersions = getTestableVersionList(project, updatePluginExtension)
        allVersions.subList(1, allVersions.size())
    }

    def static getDatabaseConnectionSystemProperties(Project project) {
        DatabasePluginExtension configuration = project.extensions.getByType(DatabasePluginExtension.class)
        def dbValues = [
                "db.vendor"       : String.valueOf(configuration.dbvendor),
                "db.url"          : String.valueOf(configuration.dburl),
                "db.user"         : String.valueOf(configuration.dbuser),
                "db.password"     : String.valueOf(configuration.dbpassword),
                "db.driverClass"  : String.valueOf(configuration.dbdriverClass),
                "db.root.user"    : String.valueOf(configuration.dbRootUser),
                "db.root.password": String.valueOf(configuration.dbRootPassword),
                "db.server.name"  : String.valueOf(configuration.dbServerName),
                "db.server.port"  : String.valueOf(configuration.dbServerPort),
                "db.database.name": String.valueOf(configuration.dbDatabaseName),
                "auto.accept"     : "true"
        ]

        if ('oracle' == configuration.dbvendor) {
            // fix for https://community.oracle.com/message/3701989
            // http://www.thezonemanager.com/2015/07/whats-so-special-about-devurandom.html
            dbValues.put('java.security.egd', 'file:/dev/./urandom')
            // fix for ORA-01882
            dbValues.put('user.timezone', 'UTC')
        }

        dbValues
    }

}
