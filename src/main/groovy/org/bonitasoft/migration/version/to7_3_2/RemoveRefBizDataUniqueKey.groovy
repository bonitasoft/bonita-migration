package org.bonitasoft.migration.version.to7_3_2

import org.bonitasoft.migration.core.MigrationContext
import org.bonitasoft.migration.core.MigrationStep

import static org.bonitasoft.migration.core.MigrationStep.DBVendor.SQLSERVER

/**
 * @author Laurent Leseigneur
 */
class RemoveRefBizDataUniqueKey extends MigrationStep {
    @Override
    def execute(MigrationContext context) {
        context.databaseHelper.dropUniqueKey("ref_biz_data_inst", getUkName(context.dbVendor))
        context.databaseHelper.dropUniqueKey("arch_ref_biz_data_inst", "uk_arch_ref_biz_data_inst")
    }

    @Override
    String getDescription() {
        return "remove unique constraint on table ref_biz_data_inst and arch_ref_biz_data_inst "
    }

    def getUkName(DBVendor dbVendor) {
        def ukName
        if (SQLSERVER == dbVendor) {
            ukName = "uk_ref_biz_data"
        } else {
            ukName = "uk_ref_biz_data_inst"
        }
        ukName
    }

}
