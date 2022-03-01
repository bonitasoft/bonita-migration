package org.bonitasoft.update.version.to7_13_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class UpdatePageSchema extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
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
