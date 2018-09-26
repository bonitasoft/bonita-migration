package org.bonitasoft.migration.version.to7_8_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.exception.MigrationException

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.*

/**
 * @author Danila Mazour
 */
class AddHiddenFieldToPages extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        def dbHelper = context.databaseHelper
        switch (context.dbVendor) {
            case SQLSERVER:
                dbHelper.addColumn("page", "hidden", "bit", "0", "NOT NULL")
                break
            case ORACLE:
                dbHelper.addColumn("page", "hidden", "number(1)", "0", "NOT NULL")
                break
            case POSTGRES:
                dbHelper.addColumn("page", "hidden", "boolean", "false", null)
                break
            case MYSQL:
                dbHelper.addColumn("page", "hidden", "boolean", "false", null)
                break
            default:
                throw new MigrationException("dbVendor " + context.dbVendor.toString() + " not recognized");
        }
    }

    @Override
    String getDescription() {
        return "Add a new column hidden to the page table."
    }

}
