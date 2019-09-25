package org.bonitasoft.migration.version.to7_7_5

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class WarnAboutNoEventWhenDeletingArchive extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        return null
    }

    @Override
    String getDescription() {
        return "Warn about events that are not thrown anymore when deleting archived processes instances"
    }

    @Override
    String getWarning() {
        return """Starting from 7.7.5, when you delete archived process instances, they will be deleted more efficiently in batch.
If you have custom event handlers configured that are using the following events, they will not be called anymore.
You can ignore this warning if you have no custom event handlers configured.
Events not thrown anymore are:
* ARCHIVED_FLOWNODE_INSTANCE_DELETED
* DATA_INSTANCE_DELETED
* CONNECTOR_INSTANCE_DELETED
* SADocumentMapping_DELETED
* SDocument_DELETED
* PROCESSINSTANCE_DELETED
"""
    }
}
