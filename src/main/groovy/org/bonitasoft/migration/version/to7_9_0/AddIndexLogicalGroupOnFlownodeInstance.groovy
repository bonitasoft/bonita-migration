package org.bonitasoft.migration.version.to7_9_0;

import org.bonitasoft.migration.core.MigrationContext;
import org.bonitasoft.migration.core.MigrationStep;

/**
 * @author Danila Mazour
 */
class AddIndexLogicalGroupOnFlownodeInstance  extends MigrationStep  {

    @Override
    def execute(MigrationContext context) {
        context.getDatabaseHelper().addOrReplaceIndex("flownode_instance", "idx_fni_loggroup3_terminal", "logicalgroup3", "terminal", "tenantid")
    }

    @Override
    String getDescription() {
        return "Add new index 'idx_fni_loggroup3_terminal' on 'flownode_instance' table"
    }
}
