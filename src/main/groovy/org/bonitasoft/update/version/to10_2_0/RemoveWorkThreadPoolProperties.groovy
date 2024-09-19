package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to remove work thread pool properties from configuration file
 */
class RemoveWorkThreadPoolProperties extends UpdateStep {

    static final String CONFIG_FILE_NAME = "bonita-tenant-community-custom.properties"

    @Override
    def execute(UpdateContext context) {
        context.configurationHelper.removePropertiesInConfigFiles(CONFIG_FILE_NAME, "bonita.tenant.work.corePoolSize",
                                                                                     "bonita.tenant.work.maximumPoolSize",
                                                                                     "bonita.tenant.work.keepAliveTimeSeconds")
    }

    @Override
    String getDescription() {
        return "Remove works thread pool properties"
    }
}
