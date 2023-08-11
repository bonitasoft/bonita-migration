package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddAppVersionToPlatform extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with { addColumnIfNotExist("platform", "application_version", VARCHAR(50), "'0.0.0'", "NOT NULL") }
    }

    @Override
    String getDescription() {
        return "Add column application_version to the platform table"
    }
}
