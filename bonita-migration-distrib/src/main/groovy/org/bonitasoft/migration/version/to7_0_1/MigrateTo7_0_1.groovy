package org.bonitasoft.migration.version.to7_0_1

import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.VersionMigration

/**
 * @author Baptiste Mesta
 */
class MigrateTo7_0_1 extends VersionMigration {
    @Override
    def List<MigrationStep> getMigrationSteps() {
        return [new UpdateDefaultApplicationTheme()]
    }
}
