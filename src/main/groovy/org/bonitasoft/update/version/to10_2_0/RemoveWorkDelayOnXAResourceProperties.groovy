package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to remove work delay on multiple XA resource properties
 */
class RemoveWorkDelayOnXAResourceProperties extends UpdateStep {

    static final String CONFIG_FILE_NAME = "bonita-tenant-community-custom.properties"

    @Override
    def execute(UpdateContext context) {
        context.configurationHelper.removePropertiesInConfigFiles(CONFIG_FILE_NAME, "bonita.tenant.work.sqlserver.delayOnMultipleXAResource",
                "bonita.tenant.work.oracle.delayOnMultipleXAResource",
                "bonita.tenant.work.mysql.delayOnMultipleXAResource")
    }

    @Override
    String getDescription() {
        return "Remove work delay on multiple XA resource properties"
    }
}
