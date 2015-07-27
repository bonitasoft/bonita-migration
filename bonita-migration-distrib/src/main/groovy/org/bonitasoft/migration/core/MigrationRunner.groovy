package org.bonitasoft.migration.core

import groovy.sql.Sql

/**
 * @author Baptiste Mesta
 */
class MigrationRunner {

    List<VersionMigration> versionMigrations
    Sql sql
    String dbVendor
    Logger logger

    def run() {
        versionMigrations.each {
            logger.info "Execute migration to version " + it.getClass().getSimpleName()
            it.getMigrationSteps().each { step ->
                logger.info "execute " + step.description
                step.execute(sql, MigrationStep.DBVendor.valueOf(dbVendor.toUpperCase()))
            }
        }
    }

}
