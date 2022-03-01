package org.bonitasoft.update.version.to7_13_0


import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class UpdateApplicationSchema extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
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
