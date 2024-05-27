package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to add the boolean column `advanced` to table `business_app`
 */
class AddColumnAdvancedToBusinessApp extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("business_app", "advanced", BOOLEAN(), booleanValue(false), null)
        }
    }

    @Override
    String getDescription() {
        return "Add column 'advanced' to table 'business_app'"
    }
}
