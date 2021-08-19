package org.bonitasoft.migration.version.to7_13_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class RemoveThemes extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.dropTableIfExists("theme")
        context.sql.executeUpdate("DELETE FROM sequence WHERE id = ${9890} ")
    }

    @Override
    String getDescription() {
        return "Remove theme table"
    }
}
