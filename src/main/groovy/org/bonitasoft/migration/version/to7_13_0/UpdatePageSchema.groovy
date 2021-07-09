package org.bonitasoft.migration.version.to7_13_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class UpdatePageSchema extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("page", "pageHash", VARCHAR(32), null, null)
            addColumnIfNotExist("page", "editable", BOOLEAN(), booleanValue(true), null)
            addColumnIfNotExist("page", "removable", BOOLEAN(), booleanValue(true), null)
        }
    }

    @Override
    String getDescription() {
        return "add `pageHash`, `editable` & `removable` columns in `page` table"
    }
}
