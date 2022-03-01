package org.bonitasoft.update.version.to7_12_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddIndexLG4LG2OnArchFlownodeInstance extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.getDatabaseHelper().addOrReplaceIndex("arch_flownode_instance", "idx_lg4_lg2", "tenantId", "logicalGroup4","logicalGroup2")
    }

    @Override
    String getDescription() {
        return "Add new index 'idx_lg4_lg2' on 'arch_flownode_instance' table"
    }
}
