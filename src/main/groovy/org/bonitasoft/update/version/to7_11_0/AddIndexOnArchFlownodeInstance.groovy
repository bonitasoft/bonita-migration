package org.bonitasoft.update.version.to7_11_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

class AddIndexOnArchFlownodeInstance extends UpdateStep {

    @Override
    def execute(UpdateContext context) {
        context.getDatabaseHelper().addOrReplaceIndex("arch_flownode_instance", "idx_afi_kind_lg4", "tenantId", "logicalGroup4")
    }

    @Override
    String getDescription() {
        return "Add new index 'idx_afi_kind_lg4' on 'arch_flownode_instance' table"
    }
}
