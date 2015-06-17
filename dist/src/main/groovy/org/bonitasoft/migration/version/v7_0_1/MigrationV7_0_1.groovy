package org.bonitasoft.migration.version.v7_0_1

import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.VersionMigration

/**
 * @author Baptiste Mesta
 */
class MigrationV7_0_1 extends VersionMigration{
    @Override
    def List<MigrationStep> getMigrationSteps() {
        return [new DoSomethingStep()]
    }
}
