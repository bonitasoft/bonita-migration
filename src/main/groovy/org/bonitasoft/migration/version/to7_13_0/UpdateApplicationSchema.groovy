package org.bonitasoft.migration.version.to7_13_0

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class UpdateApplicationSchema extends MigrationStep {

    def VARCHAR255 = [
            (ORACLE)   : "VARCHAR2(255 CHAR)",
            (POSTGRES) : "VARCHAR(255)",
            (MYSQL)    : "VARCHAR(255)",
            (SQLSERVER): "NVARCHAR(255)",
    ]
    def BLOB = [
            (ORACLE)   : "BLOB",
            (POSTGRES) : "BYTEA",
            (MYSQL)    : "LONGBLOB",
            (SQLSERVER): "VARBINARY(MAX)"
    ]
    def BOOLEAN = [
            (ORACLE)   : "NUMBER(1)",
            (POSTGRES) : "BOOLEAN",
            (MYSQL)    : "BOOLEAN",
            (SQLSERVER): "BIT"
    ]

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper
                .addColumnIfNotExist("business_app", "iconMimeType", VARCHAR255[context.dbVendor], null, null)
        context.databaseHelper
                .addColumnIfNotExist("business_app", "iconContent", BLOB[context.dbVendor], null, null)
        context.databaseHelper
                .addColumnIfNotExist("business_app", "editable", BOOLEAN[context.dbVendor], context.dbVendor == ORACLE ? "1" : "true", null)
        context.databaseHelper
                .addColumnIfNotExist("business_app", "internalProfile", VARCHAR255[context.dbVendor], null, null)
    }

    @Override
    String getDescription() {
        return "add `iconId` column in `business_app` table"
    }
}
