package org.bonitasoft.migration.version.to7_13_0

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class AddMimeTypeContentTypeColumnInApplication extends MigrationStep {

    def IconMimeTypeColumnType = [
            (ORACLE)   : "VARCHAR2(255)",
            (POSTGRES) : "VARCHAR(255)",
            (MYSQL)    : "VARCHAR(255)",
            (SQLSERVER):  "NVARCHAR(255)",
    ]

    def IconContentColumnType = [
            (ORACLE)   : "BLOB",
            (POSTGRES) : "BYTEA",
            (MYSQL)    : "LONGBLOB",
            (SQLSERVER): "VARBINARY(MAX)"
    ]

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper
                .addColumnIfNotExist("business_app", "iconMimeType", IconMimeTypeColumnType[context.dbVendor], null, null)
        context.databaseHelper
                .addColumnIfNotExist("business_app", "iconContent", IconContentColumnType[context.dbVendor], null, null)
    }

    @Override
    String getDescription() {
        return "add `iconId` column in `business_app` table"
    }
}
