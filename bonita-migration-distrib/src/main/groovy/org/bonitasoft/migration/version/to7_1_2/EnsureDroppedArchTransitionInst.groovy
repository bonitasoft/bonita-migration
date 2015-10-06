package org.bonitasoft.migration.version.to7_1_2

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Elias Ricken de Medeiros
 */
class EnsureDroppedArchTransitionInst extends MigrationStep {


    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.dropTableIfExists("arch_transition_instance")
    }

    @Override
    String getDescription() {
        return "drop table arch_transition_instance if exists"
    }
}
