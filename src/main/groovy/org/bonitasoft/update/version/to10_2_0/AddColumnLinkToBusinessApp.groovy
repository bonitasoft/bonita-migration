package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to add the boolean column `isLink` to table `business_app`
 */
class AddColumnLinkToBusinessApp extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("business_app", "isLink", BOOLEAN(), booleanValue(false), null)
        }
    }

    @Override
    String getDescription() {
        return "Add column 'isLink' to table 'business_app'"
    }
}
