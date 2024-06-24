package org.bonitasoft.update.version.to10_2_0

import org.bonitasoft.update.core.UpdateContext
import org.bonitasoft.update.core.UpdateStep

/**
 * Update step to add the boolean column `link` to table `business_app`
 */
class AddColumnLinkToBusinessApp extends UpdateStep {
    @Override
    def execute(UpdateContext context) {
        context.databaseHelper.with {
            addColumnIfNotExist("business_app", "link", BOOLEAN(), booleanValue(false), null)
        }
    }

    @Override
    String getDescription() {
        return "Add column 'link' to table 'business_app'"
    }
}
