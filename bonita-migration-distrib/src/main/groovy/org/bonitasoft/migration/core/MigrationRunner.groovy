package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class MigrationRunner {

    List<VersionMigration> versionMigrations
    MigrationContext context
    Logger logger

    def run() {
        context.openSqlConnection()
        versionMigrations.each {
            logger.info "Execute migration to version " + it.getVersion()
            it.context = context
            it.migrateBonitaHome()
            it.getMigrationSteps().each { step ->
                logger.info "execute " + step.description
                step.execute(context)
            }
            changePlatformVersion(context.sql, it.getVersion())
        }
        context.closeSqlConnection()
    }

    def changePlatformVersion(Sql sql, String version) {
        sql.executeUpdate("UPDATE platform SET previousVersion = version");
        sql.executeUpdate("UPDATE platform SET version = $version")
        logger.info("Platform version in database changed to $version")
    }

}
