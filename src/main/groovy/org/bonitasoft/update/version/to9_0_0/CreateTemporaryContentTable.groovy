package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class CreateTemporaryContentTable extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.executeScript("CreateTemporaryContentTable", "")
    }

    @Override
    String getDescription() {
        return "Add table for temporary content"
    }
}
