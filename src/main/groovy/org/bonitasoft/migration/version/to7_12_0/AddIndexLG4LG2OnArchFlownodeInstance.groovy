package org.bonitasoft.migration.version.to7_12_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

class AddIndexLG4LG2OnArchFlownodeInstance extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.getDatabaseHelper().addOrReplaceIndex("arch_flownode_instance", "idx_lg4_lg2", "tenantId", "logicalGroup4","logicalGroup2")
    }

    @Override
    String getDescription() {
        return "Add new index 'idx_lg4_lg2' on 'arch_flownode_instance' table"
    }
}
