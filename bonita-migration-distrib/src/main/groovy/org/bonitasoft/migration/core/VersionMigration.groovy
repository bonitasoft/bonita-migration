package org.bonitasoft.migration.core

/**
 * @author Baptiste Mesta
 */
abstract class VersionMigration {

    abstract List<MigrationStep> getMigrationSteps()
}
