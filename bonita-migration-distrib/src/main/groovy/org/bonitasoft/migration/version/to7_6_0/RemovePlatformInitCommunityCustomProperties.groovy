package org.bonitasoft.migration.version.to7_6_0

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep
import org.bonitasoft.migration.core.database.ConfigurationHelper

/**
 * @author Danila Mazour
 */
class RemovePlatformInitCommunityCustomProperties extends MigrationStep {

    @Override
    def execute(MigrationContext context) {
        context.configurationHelper.deleteConfigurationFile("bonita-platform-init-community-custom.properties", 0, "PLATFORM_INIT_ENGINE")
    }

    @Override
    String getDescription() {
        return "Remove unused bonita-platform-init-community.properties file"
    }
}
