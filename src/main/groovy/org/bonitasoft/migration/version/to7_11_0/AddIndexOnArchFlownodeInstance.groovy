package org.bonitasoft.migration.version.to7_11_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class AddIndexOnArchFlownodeInstance extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.getDatabaseHelper().addOrReplaceIndex("arch_flownode_instance", "idx_afi_kind_lg4", "tenantId", "logicalGroup4")
    }

    @Override
    String getDescription() {
        return "Add new index 'idx_afi_kind_lg4' on 'arch_flownode_instance' table"
    }
}
