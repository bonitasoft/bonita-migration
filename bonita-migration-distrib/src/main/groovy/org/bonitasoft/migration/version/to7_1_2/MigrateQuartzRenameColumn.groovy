package org.bonitasoft.migration.version.to7_1_2

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

/**
 * @author Laurent Leseigneur
 */
class MigrateQuartzRenameColumn extends MigrationStep {


    public static final String QRTZ_SIMPROP_TRIGGERS = "qrtz_simprop_triggers"

    @Override
    def execute(MigrationContext context) {
        if (context.dbVendor.equals(DBVendor.POSTGRES)) {
            def helper = context.databaseHelper
            if (helper.hasColumnOnTable(QRTZ_SIMPROP_TRIGGERS, "int8_prop_1")) {
                helper.renameColumn(QRTZ_SIMPROP_TRIGGERS, "int8_prop_1", "long_prop_1", "int8")
            }
            if (helper.hasColumnOnTable(QRTZ_SIMPROP_TRIGGERS, "int8_prop_2")) {
                helper.renameColumn(QRTZ_SIMPROP_TRIGGERS, "int8_prop_2", "long_prop_2", "int8")
            }
        }
    }


    @Override
    String getDescription() {
        return "rename columns in qrtz_simprop_triggers (postgres only)"
    }

}
