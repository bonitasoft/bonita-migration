package org.bonitasoft.migration.version.to7_10_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Danila Mazour
 */
class ReplaceIndexMysqlUTF8MB4Compatibility extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        if (MigrationStep.DBVendor.MYSQL.equals(context.dbVendor)) {
            context.getDatabaseHelper().addOrReplaceIndex("message_instance", "idx_message_instance", "messageName", "targetProcess", "correlation1", "correlation2")
        }
    }

    @Override
    String getDescription() {
        return "Correct index 'idx_message_instance' on mysql by removing the correlation3 column, for utf8-mb4 encoding compatibility"
    }


}
