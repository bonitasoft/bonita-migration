package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class AddMessageAndWaitingEventDBIndices extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.addOrReplaceIndex("waiting_event", "idx_waiting_event_correl", "correlation1", "correlation2", "correlation3", "correlation4", "correlation5")
        context.databaseHelper.addOrReplaceIndex("message_instance", "idx_message_instance_correl", "correlation1", "correlation2", "correlation3", "correlation4", "correlation5")
    }


    @Override
    String getDescription() {
        return "Add new database indices on correlation keys for tables 'waiting_event' and 'message_instance'"
    }


}
