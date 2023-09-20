package org.bonitasoft.update.version.to9_0_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddMaintenanceMessageToPlatform extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("platform", "maintenance_message", TEXT(), null, null)
            addColumnIfNotExist("platform", "maintenance_message_active", BOOLEAN(), booleanValue(false), "NOT NULL")
        }
    }

    @Override
    String getDescription() {
        return "Add columns maintenance_message & maintenance_message_active to the platform table"
    }
}
