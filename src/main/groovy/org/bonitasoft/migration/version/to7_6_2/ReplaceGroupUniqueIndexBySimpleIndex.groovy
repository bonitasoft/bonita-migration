package org.bonitasoft.migration.version.to7_6_2

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Danila Mazour
 */
class ReplaceGroupUniqueIndexBySimpleIndex extends MigrationStep {

    @Override
    execute(MigrationContext context) {
        context.databaseHelper.dropUniqueKey("group_", context.databaseHelper.getUniqueKeyNameOnTable("group_"))
        context.databaseHelper.addOrReplaceIndex("group_", "idx_group_name", "tenantid", "parentPath", "name")
    }

    @Override
    String getDescription() {
        return "Update Group table : replace unique constraint (tenantid, parentPath, name) by an index on (tenantid, parentPath, name) "
    }
}
