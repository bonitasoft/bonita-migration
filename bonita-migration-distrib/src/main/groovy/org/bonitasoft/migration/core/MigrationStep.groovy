package org.bonitasoft.migration.core
/**
 * @author Baptiste Mesta
 */
abstract class MigrationStep {

    enum DBVendor { ORACLE, POSTGRES, MYSQL, SQLSERVER}

    abstract execute(MigrationContext context)

    abstract String getDescription()


}
