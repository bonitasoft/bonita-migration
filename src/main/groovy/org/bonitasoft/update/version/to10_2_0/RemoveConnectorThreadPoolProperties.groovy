package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to remove connector thread pool properties from configuration file
 */
class RemoveConnectorThreadPoolProperties extends UpdateStep {

    static final String CONFIG_FILE_NAME = "bonita-tenant-community-custom.properties"

    @Override
    def execute(UpdateContext context) {
        context.configurationHelper.removePropertiesInConfigFiles(CONFIG_FILE_NAME, "bonita.tenant.connector.corePoolSize",
                                                                                     "bonita.tenant.connector.maximumPoolSize",
                                                                                     "bonita.tenant.connector.keepAliveTimeSeconds")
    }

    @Override
    String getDescription() {
        return "Remove connector thread pool properties"
    }
}
