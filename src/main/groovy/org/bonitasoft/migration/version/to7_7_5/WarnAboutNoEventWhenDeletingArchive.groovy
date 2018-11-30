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
        return """Because archived process instances are now deleted in batch,
some events related to deletion of archived elements are not thrown anymore: 
* Delete archived flow node instance: ARCHIVED_FLOWNODE_INSTANCE_DELETED
* Delete archived data instance: DATA_INSTANCE_DELETED
* Delete archived connector instance: CONNECTOR_INSTANCE_DELETED
* Delete archived document mapping: SADocumentMapping_DELETED
* Delete document (when deleting archived document mapping): SDocument_DELETED
* Delete archived process instance: PROCESSINSTANCE_DELETED
"""
    }
}
