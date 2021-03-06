package org.bonitasoft.migration.plugin.db

import org.bonitasoft.migration.plugin.MigrationPlugin
import org.bonitasoft.migration.plugin.MigrationPluginExtension
import org.gradle.api.Project

import static org.bonitasoft.migration.plugin.VersionUtils.underscored
import static org.bonitasoft.migration.plugin.VersionUtils.getTestableVersionList

class DatabaseResourcesConfigurator {

    private static final String SYS_PROP_DB_URL = "db.url"

    def static configureDatabaseResources(Project project) {
        String dbVendor = System.getProperty("db.vendor", "postgres")
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        extension.dbvendor = dbVendor
        if (shouldUseDockerDb()) {
            project.logger.quiet("no system property db.url set, using docker to test on $dbVendor")
            DockerDatabaseContainerTasksCreator.createTasks(project)
            extension.isDockerDb = true
        }
        else {
            // for docker database containers that have no xa transaction support like ancient versions of sqlserver, rely on an external database:
            project.logger.quiet 'Not using docker database, set the database properties using system properties'
            extension.dburl = System.getProperty(SYS_PROP_DB_URL, "jdbc:sqlserver://sqlserver2.rd.lan:1533;database=mig_ci")
            extension.dbuser = System.getProperty("db.user", "mig_ci")
            extension.dbpassword = System.getProperty("db.password", "mig_ci")
            extension.dbdriverClass = System.getProperty("db.driverClass", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
            extension.dbRootUser = System.getProperty("db.root.user", "sa")
            extension.dbRootPassword = System.getProperty("db.root.password", "Bonita12")

            DbParser.DbConnectionSettings dbConnectionSettings = new DbParser().extractDbConnectionSettings(extension.dburl)
            extension.dbServerName = dbConnectionSettings.serverName
            extension.dbServerPort = dbConnectionSettings.portNumber
            extension.dbDatabaseName = dbConnectionSettings.databaseName

            project.logger.quiet("db.url set to ${extension.dburl}")
            project.logger.quiet("db.vendor set to ${extension.dbvendor}")
            project.logger.quiet("db.driver set to ${extension.dbdriverClass}")
        }
    }

    private static boolean shouldUseDockerDb() {
        System.getProperty(SYS_PROP_DB_URL) == null
    }

    def static finalizeTasksDependenciesOnDatabaseResources(Project project) {
        DatabasePluginExtension extension = project.extensions.getByType(DatabasePluginExtension.class)
        def integrationTestTask = project.tasks.findByName('integrationTest')
        if (extension.isDockerDb) {
            def uniqueName = "${extension.dbvendor.capitalize()}"
            def vendorConfigurationTask = project.tasks.findByName("${uniqueName}Configuration")
            def removeVendorContainer = project.tasks.findByName("remove${uniqueName}Container")

            // Integration tests
            integrationTestTask.dependsOn(vendorConfigurationTask)
            removeVendorContainer.mustRunAfter(integrationTestTask)

            // Migration tests
            if (project.plugins.hasPlugin(MigrationPlugin)) {
                getVersionsToMigrate(project).each {
                    String underscoredVersion = underscored(it)
                    project.tasks.findByName("cleandb_" + underscoredVersion).dependsOn(vendorConfigurationTask)
                    removeVendorContainer.mustRunAfter(project.tasks.findByName("testMigration_" + underscoredVersion))
                }
            }
        }
        else {
            // Integration tests
            MigrationPluginExtension migrationPluginExtension  = project.extensions.getByType(MigrationPluginExtension.class)
            CleanDbTask cleanDb = project.task("cleandb_it_" + (migrationPluginExtension.isSP ? "com" : "org"), type: CleanDbTask)
            integrationTestTask.dependsOn cleanDb

            CleanDbTask dropDb = project.task("dropDb_it_" + (migrationPluginExtension.isSP ? "com" : "org"), type: CleanDbTask)
            dropDb.setDropOnly(true)
            integrationTestTask.finalizedBy(dropDb)

            // Migration tests
            getVersionsToMigrate(project).each {
                String underscoredVersion = underscored(it)
                CleanDbTask dropDbMigration = project.task("dropDb_" + underscoredVersion, type: CleanDbTask)
                dropDbMigration.setDropOnly(true)
                project.tasks.findByName("testMigration_" + underscoredVersion).finalizedBy(dropDbMigration)
            }
        }
    }

    private static List<String> getVersionsToMigrate(Project project) {
        MigrationPluginExtension migrationPluginExtension = project.extensions.getByType(MigrationPluginExtension.class)
        def allVersions = getTestableVersionList(project, migrationPluginExtension)
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

        if ('oracle'.equals(configuration.dbvendor)) {
            // fix for https://community.oracle.com/message/3701989
            // http://www.thezonemanager.com/2015/07/whats-so-special-about-devurandom.html
            dbValues.put('java.security.egd', 'file:/dev/./urandom')
            // fix for ORA-01882
            dbValues.put('user.timezone', 'UTC')
        }

        dbValues
    }

}
