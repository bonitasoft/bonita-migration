package org.bonitasoft.migration.version.to7_3_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Emmanuel Duchastenier
 */
class DropOrphanWaitingEvents extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.sql.execute("DELETE FROM waiting_event WHERE processdefinitionid NOT IN (SELECT DISTINCT processid FROM process_definition)")
    }

    @Override
    String getDescription() {
        return "drops rows in WAITING_EVENTS that refer to no-longer existing processes"
    }
}
