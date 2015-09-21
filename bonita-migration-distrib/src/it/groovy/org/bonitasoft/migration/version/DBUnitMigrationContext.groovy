package org.bonitasoft.migration.version

import groovy.sql.Sql
import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.DatabaseHelper

/**
 * @author Elias Ricken de Medeiros
 */
class DBUnitMigrationContext extends  MigrationContext {


    DBUnitMigrationContext(Sql sql) {
        this.sql = sql

        def dBVendor = MigrationStep.DBVendor.valueOf(System.getProperty("dbvendor", "POSTGRES").toUpperCase())
        this.databaseHelper = new DatabaseHelper(dbVendor: dBVendor, sql: sql)
    }

    @Override
    def openSqlConnection() {
        // nothing to do
    }
}
