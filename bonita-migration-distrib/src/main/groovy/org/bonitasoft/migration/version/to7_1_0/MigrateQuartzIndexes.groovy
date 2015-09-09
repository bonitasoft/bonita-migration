package org.bonitasoft.migration.version.to7_1_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class MigrateQuartzIndexes extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.executeScript("QuartzIndexes", "quartz")
    }


    @Override
    String getDescription() {
        return "add indexes on Quartz tables"
    }
}
