package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class CreateTemporaryContentTable extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.executeScript("CreateTemporaryContentTable", "")
        context.databaseHelper.insertSequences(Map.of(-1L, 1L) , 5)
    }

    @Override
    String getDescription() {
        return "Add table for temporary content"
    }
}
