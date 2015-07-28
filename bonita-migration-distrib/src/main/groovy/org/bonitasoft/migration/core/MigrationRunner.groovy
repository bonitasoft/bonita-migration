package org.bonitasoft.migration.core
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
        }
        context.closeSqlConnection()
    }

}
