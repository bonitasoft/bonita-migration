package org.bonitasoft.migration.version.to7_13_0


import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class UpdateApplicationSchema extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("business_app", "iconMimeType", VARCHAR(255), null, null)
            addColumnIfNotExist("business_app", "iconContent", BLOB(), null, null)
            addColumnIfNotExist("business_app", "editable", BOOLEAN(), booleanValue(true), null)
            addColumnIfNotExist("business_app", "internalProfile", VARCHAR(255), null, null)
        }

    }

    @Override
    String getDescription() {
        return "add new columns in `business_app` table"
    }
}
