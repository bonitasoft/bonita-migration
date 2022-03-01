package org.bonitasoft.update.version.to7_13_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class RemoveThemes extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.dropTableIfExists("theme")
        context.sql.executeUpdate("DELETE FROM sequence WHERE id = ${9890} ")
    }

    @Override
    String getDescription() {
        return "Remove theme table"
    }
}
