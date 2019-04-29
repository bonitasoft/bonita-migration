package org.bonitasoft.migration.version.to7_9_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Danila Mazour
 */
class AddIndexActivityKindOnFlownodeInstance extends MigrationStep {


    @Override
    Object execute(MigrationContext context) {

        return context.getDatabaseHelper().addOrReplaceIndex("flownode_instance", "idx_fni_activity_instance_id_kind", "activityInstanceId", "kind", "tenantid")

    }

    @Override
    String getDescription() {
        return "Add new index 'idx_fni_activity_instance_id_kind' on 'flownode_instance' table"
    }
}
