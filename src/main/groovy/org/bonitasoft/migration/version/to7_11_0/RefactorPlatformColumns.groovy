package org.bonitasoft.migration.version.to7_11_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class RefactorPlatformColumns extends MigrationStep {

    public static final String PLATFORM_TABLE = "platform"

    @Override
    def execute(MigrationContext context) {
        def helper = context.databaseHelper
        if (helper.hasColumnOnTable(PLATFORM_TABLE, "initialVersion")) {
            // last parameter is only used by MySQL
            helper.renameColumn(PLATFORM_TABLE, 'initialVersion', 'initial_bonita_version', 'VARCHAR(50)')
        }
        if (helper.hasColumnOnTable(PLATFORM_TABLE, "createdBy")) {
            // last parameter is only used by MySQL
            helper.renameColumn(PLATFORM_TABLE, 'createdBy', 'created_by', 'VARCHAR(50)')
        }
        helper.dropColumnIfExists(PLATFORM_TABLE, "previousVersion")
    }

    @Override
    String getDescription() {
        "Refactor columns in 'platform' table to better server database version scheme"
    }
}
